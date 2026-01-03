import { createTreeWithEmptyWorkspace } from '@nx/devkit/testing';
import { Tree, readProjectConfiguration } from '@nx/devkit';

import { tagUiLibsGenerator } from './tag-ui-libs';
import { TagUiLibsGeneratorSchema } from './schema';

describe('tag-ui-libs generator', () => {
  let tree: Tree;
  const options: TagUiLibsGeneratorSchema = { name: 'test' };

  beforeEach(() => {
    tree = createTreeWithEmptyWorkspace();
  });

  it('should run successfully', async () => {
    await tagUiLibsGenerator(tree, options);
    const config = readProjectConfiguration(tree, 'test');
    expect(config).toBeDefined();
  });
});
