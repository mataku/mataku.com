import wasmModule from './build/compileSync/wasmJs/main/productionExecutable/kotlin/blog-worker.wasm';
import { importObject, setWasmExports } from './build/compileSync/wasmJs/main/productionExecutable/kotlin/blog-worker.import-object.mjs';

const instance = await WebAssembly.instantiate(wasmModule, importObject);
setWasmExports(instance.exports);
const { fetch: workerFetch } = instance.exports;

export default {
  fetch(request, env) {
    return workerFetch(request, env);
  }
};
