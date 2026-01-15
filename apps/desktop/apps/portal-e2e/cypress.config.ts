import { nxE2EPreset } from "@nx/cypress/plugins/cypress-preset";
import { defineConfig } from "cypress";

export default defineConfig({
  e2e: {
    baseUrl: "http://localhost:4200",
    ...nxE2EPreset(__filename, {
      cypressDir: "cypress",
      webServerCommands: {
        default: "nx run portal:serve",
        production: "nx run portal:serve:production",
      },
      ciWebServerCommand: "nx run portal:serve-static",
    }),
  },

  component: {
    devServer: {
      framework: "angular",
      bundler: "webpack",
    },
    specPattern: "**/*.cy.ts",
  },
});
