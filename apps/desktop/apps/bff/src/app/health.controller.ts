import { Controller, Get, Req, Res } from '@nestjs/common';
import { Request, Response } from 'express';
import { BffService } from './bff.service';
import { serverStatusResponseSchema } from './schemas';

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
      schema: serverStatusResponseSchema,
    });
  }

  @Get('ready')
  async ready(@Req() req: Request, @Res() res: Response) {
    return this.bff.forward({
      req,
      res,
      path: '/ready',
      method: 'GET',
      schema: serverStatusResponseSchema,
    });
  }
}
