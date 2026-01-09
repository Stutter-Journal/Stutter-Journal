import { Controller, Get, Param, Query, Req, Res } from '@nestjs/common';
import { Request, Response } from 'express';
import { contractsZod } from '@org/contracts';
import { BffService } from './bff.service';
import { serverErrorResponseSchema } from './schemas';

@Controller('patients')
export class PatientsController {
  constructor(private readonly bff: BffService) {}

  @Get()
  async list(@Req() req: Request, @Res() res: Response) {
    return this.bff.forward({
      req,
      res,
      path: '/patients',
      method: 'GET',
      schema: contractsZod.getPatientsResponse,
      schemasByStatus: {
        200: contractsZod.getPatientsResponse,
        401: serverErrorResponseSchema,
      },
    });
  }

  @Get(':id/entries')
  async entries(
    @Param('id') id: string,
    @Query('from') from: string | undefined,
    @Query('to') to: string | undefined,
    @Req() req: Request,
    @Res() res: Response,
  ) {
    const search = new URLSearchParams();
    if (from) search.append('from', from);
    if (to) search.append('to', to);
    const query = search.toString();
    const path = query
      ? `/patients/${id}/entries?${query}`
      : `/patients/${id}/entries`;

    return this.bff.forward({
      req,
      res,
      path,
      method: 'GET',
      schema: contractsZod.getPatientsIdEntriesResponse,
      schemasByStatus: {
        200: contractsZod.getPatientsIdEntriesResponse,
        400: serverErrorResponseSchema,
        401: serverErrorResponseSchema,
        403: serverErrorResponseSchema,
      },
    });
  }
}
