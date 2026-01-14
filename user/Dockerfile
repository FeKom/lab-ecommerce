FROM oven/bun AS build
WORKDIR /app

COPY package.json package.json
RUN bun install --ignore-scripts
COPY tsconfig.json tsconfig.json
COPY ./src ./src
COPY ./build.ts ./build.ts
RUN bun build.ts

FROM gcr.io/distroless/base
WORKDIR /app
COPY --from=build /app/build/server server
ENV NODE_ENV=production
CMD ["./server"]
EXPOSE 8085
