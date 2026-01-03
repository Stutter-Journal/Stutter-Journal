import { betterAuth } from 'better-auth';
import { toNodeHandler } from 'better-auth/node';
import type { BetterAuthOptions } from 'better-auth';

/**
 * Create the Better Auth handler for Nest/Express. Provide a database adapter
 * (drizzle/prisma/kysely) via the options below. The handler is mounted under
 * /api/auth in main.ts and will issue/validate the session cookie.
 */
export function createBetterAuthHandler() {
  const betterAuthOptions: BetterAuthOptions = {
    // Set to the mount path minus the global "api" prefix (we mount at /api/auth)
    basePath: '/auth',
    baseURL: process.env.BETTER_AUTH_BASE_URL,

    // TODO: Provide your database adapter, e.g. drizzleAdapter(db, { provider: 'pg' })
    // or prismaAdapter(prisma). Leaving this undefined will throw at startup.
    database: undefined as unknown as BetterAuthOptions['database'],

    // Cookie configuration goes inside 'advanced'
    advanced: {
      useSecureCookies: process.env.NODE_ENV === 'production',
    },
  };

  if (!betterAuthOptions.database) {
    throw new Error(
      'Better Auth database adapter is not configured. Update better-auth.handler.ts to supply one.',
    );
  }

  const auth = betterAuth(betterAuthOptions);
  return toNodeHandler(auth);
}
