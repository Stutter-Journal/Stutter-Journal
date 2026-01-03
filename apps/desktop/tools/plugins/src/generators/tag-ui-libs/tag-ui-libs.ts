import {
  Tree,
  formatFiles,
  getProjects,
  updateProjectConfiguration,
  logger,
} from '@nx/devkit';
import { TagUiLibsGeneratorSchema } from './schema';

export async function tagUiLibsGenerator(
  tree: Tree,
  options: TagUiLibsGeneratorSchema,
) {
  const projects = getProjects(tree);
  let updatedCount = 0;

  projects.forEach((projectConfig, projectName) => {
    const projectRoot = projectConfig.root;

    // Check if project matches the pattern
    const matchesPattern = projectRoot.includes('libs/ui/');

    if (matchesPattern) {
      // Get existing tags or initialize empty array
      const existingTags = projectConfig.tags || [];

      // Add new tags that don't already exist
      const newTags = options.tags.filter((tag) => !existingTags.includes(tag));

      if (newTags.length > 0) {
        projectConfig.tags = [...existingTags, ...newTags];

        if (!options.dryRun) {
          updateProjectConfiguration(tree, projectName, projectConfig);
        }

        logger.info(
          `${options.dryRun ? '[DRY RUN] ' : ''}Tagged ${projectName} with: ${newTags.join(', ')}`,
        );
        updatedCount++;
      } else {
        logger.info(`${projectName} already has all specified tags`);
      }
    }
  });

  if (!options.dryRun) {
    await formatFiles(tree);
  }

  logger.info(
    `${options.dryRun ? '[DRY RUN] ' : ''}Updated ${updatedCount} project(s)`,
  );
}

export default tagUiLibsGenerator;
