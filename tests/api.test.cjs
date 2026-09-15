const assert = require('node:assert/strict');
const { readFileSync } = require('node:fs');
const { stripTypeScriptTypes } = require('node:module');
const { test } = require('node:test');
const { createContext, SourceTextModule, SyntheticModule } = require('node:vm');

async function loadApi(nativeModule, web = false) {
  const context = createContext();
  const source = readFileSync(require.resolve(web ? '../src/index.web.ts' : '../src/index.ts'), 'utf8');
  const module = new SourceTextModule(stripTypeScriptTypes(source), { context });
  await module.link(() => new SyntheticModule(['default'], function () {
    this.setExport('default', nativeModule);
  }, { context }));
  await module.evaluate();
  return module.namespace;
}

test('availability preserves both native results', async () => {
  for (const available of [true, false]) {
    const api = await loadApi({ isAvailableAsync: async () => available });
    assert.equal(await api.isAvailableAsync(), available);
  }
});

test('missing native module or availability method returns false', async () => {
  for (const native of [null, {}]) {
    const api = await loadApi(native);
    assert.equal(await api.isAvailableAsync(), false);
  }
  const api = await loadApi(null);
  await assert.rejects(api.requestReview(), /native module not available/);
});

test('native availability failures reach the caller', async () => {
  const failure = new Error('package manager unavailable');
  const api = await loadApi({ isAvailableAsync: async () => { throw failure; } });
  await assert.rejects(api.isAvailableAsync(), error => error === failure);
});

test('request waits for native completion and preserves rejection', async () => {
  let finish;
  const nativeCompletion = new Promise(resolve => { finish = resolve; });
  const api = await loadApi({ requestReview: () => nativeCompletion });
  let completed = false;
  const request = api.requestReview().then(() => { completed = true; });
  await Promise.resolve();
  assert.equal(completed, false);
  finish();
  await request;
  assert.equal(completed, true);

  const failure = new Error('no foreground scene');
  const failingApi = await loadApi({ requestReview: async () => { throw failure; } });
  await assert.rejects(failingApi.requestReview(), error => error === failure);
});

test('web is unavailable and rejects review without importing a native module', async () => {
  const api = await loadApi(null, true);
  assert.equal(await api.isAvailableAsync(), false);
  await assert.rejects(api.requestReview(), /not supported on web/);
});
