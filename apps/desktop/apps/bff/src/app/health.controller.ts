import { Controller, Get, Req, Res } from '@nestjs/common';
import { Request, Response } from 'express';
import { contractsZod } from '@eloquia/shared/api/contracts';
import { BffService } from './bff.service';

@Controller()
export class HealthController {
  constructor(private readonly bff: BffService) {}

  @Get('health')
  async health(@Req() req: Request, @Res() res: Response) {
    return this.bff.forward({
      req,
      res,
      path: '/health',
      method: 'GET',
      schema: contractsZod.getHealthResponse,
      schemasByStatus: {
        200: contractsZod.getHealthResponse,
      },
    });
  }

  @Get('ready')
  async ready(@Req() req: Request, @Res() res: Response) {
    return this.bff.forward({
      req,
      res,
      path: '/ready',
      method: 'GET',
      schema: contractsZod.getReadyResponse,
      schemasByStatus: {
        200: contractsZod.getReadyResponse,
        503: contractsZod.getReadyResponse,
      },
    });
  }
}
