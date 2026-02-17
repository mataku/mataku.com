import { fetch } from './build/compileSync/wasmJs/main/productionExecutable/kotlin/blog-worker.mjs';

export default {
  fetch(request, env, ctx) {
    return fetch(request, env);
  }
};
