const ROOT = build({
	module: "com.s8.core.web.helium",
	dependencies: [
		"S8-api",
		"S8-core-io-xml",
		"S8-core-io-bytes",
		"S8-core-io-joos",
		"S8-core-arch-silicon"
	],
	target: "S8-core-web-helium"
});