const input =
  process.env.ELOQUIA_OPENAPI_URL || 'http://localhost:8080/docs/doc.json';

/**
 * Orval configuration for generating typed API clients and models.
 * Set ELOQUIA_OPENAPI_URL to the live OpenAPI document (e.g. http://localhost:8080/openapi.json)
 * when you want to regenerate from the running backend.
 */
const path = require('path');

const target = path.resolve(
  __dirname,
  '../..',
  'libs/shared/api/contracts/src/generated/index.ts',
);

module.exports = {
  eloquia: {
    input,
    output: {
      target,
      client: 'fetch',
      baseUrl: process.env.ELOQUIA_API_BASE_URL || 'http://localhost:8080',
      clean: true,
    },
  },
};
