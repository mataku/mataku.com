import { bindings, defineConfig } from "cf/config";

export default defineConfig({
	worker: {
		name: "mataku-com",
		compatibilityDate: "2026-02-05",
		entrypoint: "worker/entry.js",
		observability: {
			enabled: true,
		},
		assets: {
			runWorkerFirst: false,
		},
		env: {
			ASSETS: bindings.assets(),
		},
	},
});
