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
			runWorkerFirst: ["/", "/articles/*", "!/articles/*/*"],
		},
		env: {
			ASSETS: bindings.assets(),
		},
	},
});
