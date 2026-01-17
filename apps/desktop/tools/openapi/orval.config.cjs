const input =
  process.env.ELOQUIA_OPENAPI_URL || 'http://localhost:8080/docs/doc.json';

/**
 * Orval configuration for generating typed API clients and models.
 * Set ELOQUIA_OPENAPI_URL to the live OpenAPI document (e.g. http://localhost:8080/openapi.json)
 * when you want to regenerate from the running backend.
 */
const path = require('path');

module.exports = {
  eloquia: {
    input,
    output: {
      target: path.resolve(
        __dirname,
        '../..',
        'libs/shared/api/contracts/src/generated/index.ts',
      ),
      client: 'fetch',
      baseUrl: process.env.ELOQUIA_API_BASE_URL || 'http://localhost:8080',
      clean: false, // keep zod sidecar in same folder
    },
  },
  eloquiaZod: {
    input,
    output: {
      client: 'zod',
      mode: 'single',
      target: path.resolve(
        __dirname,
        '../..',
        'libs/shared/api/contracts/src/generated/zod.ts',
      ),
      clean: false, // do not delete fetch client when generating zod
    },
  },
};
