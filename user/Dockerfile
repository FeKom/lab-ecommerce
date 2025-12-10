FROM oven/bun as build
WORKDIR /app

COPY package.json package.json
COPY bun.lock bun.lock
COPY tsconfig.json tsconfig.json
RUN bun install
COPY ./src ./src
COPY ./build.ts ./build.ts
RUN bun build.ts

FROM gcr.io/distroless/base
WORKDIR /app
COPY --from=build /app/build/server server
ENV NODE_ENV=production
CMD ["./server"]
EXPOSE 3000
