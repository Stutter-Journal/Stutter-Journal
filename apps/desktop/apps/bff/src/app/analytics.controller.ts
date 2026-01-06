import { Controller, Get, Param, Query, Req, Res } from '@nestjs/common';
import { Request, Response } from 'express';
import { BffService } from './bff.service';
import { serverAnalyticsResponseSchema } from './schemas';

@Controller('patients/:id/analytics')
export class AnalyticsController {
  constructor(private readonly bff: BffService) {}

  @Get()
  async analytics(
    @Param('id') id: string,
    @Query('range') range: string | undefined,
    @Req() req: Request,
    @Res() res: Response,
  ) {
    const search = new URLSearchParams();
    if (range) search.append('range', range);
    const query = search.toString();
    const path = query
      ? `/patients/${id}/analytics?${query}`
      : `/patients/${id}/analytics`;

    return this.bff.forward({
      req,
      res,
      path,
      method: 'GET',
      schema: serverAnalyticsResponseSchema,
    });
  }
}
