import blog from './build/compileSync/js/main/productionExecutable/kotlin/blog-worker.js';

export default {
  fetch(request, env, ctx) {
    return blog.fetch(request, env);
  }
};
