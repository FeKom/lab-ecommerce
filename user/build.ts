await Bun.build({
  entrypoints: ["./src/index.ts"],
  outdir: "./build",
  sourcemap: true,
  target: "bun",
  minify: {
    whitespace: true,
    syntax: true,
  },
  compile: {
    target: "bun-linux-arm64",
    outfile: "server",
  },
});

export {};
