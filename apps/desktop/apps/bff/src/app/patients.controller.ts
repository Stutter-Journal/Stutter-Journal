import { Controller, Get, Param, Query, Req, Res } from '@nestjs/common';
import { Request, Response } from 'express';
import { contractsZod, schemas } from '@org/contracts';
import { BffService } from './bff.service';

@Controller('patients')
export class PatientsController {
  constructor(private readonly bff: BffService) {}

  @Get()
  async list(
    @Query('search') search: string | undefined,
    @Req() req: Request,
    @Res() res: Response,
  ) {
    const qs = new URLSearchParams();
    if (search) qs.append('search', search);
    const path = qs.toString() ? `/patients?${qs.toString()}` : '/patients';

    return this.bff.forward({
      req,
      res,
      path,
      method: 'GET',
      schema: contractsZod.getPatientsResponse,
      schemasByStatus: {
        200: contractsZod.getPatientsResponse,
        401: schemas.serverErrorResponseSchema,
      },
    });
  }

  // TODO: update call signature @see list method
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
        400: schemas.serverErrorResponseSchema,
        401: schemas.serverErrorResponseSchema,
        403: schemas.serverErrorResponseSchema,
      },
    });
  }
}
