import { Body, Controller, Param, Post, Req, Res } from '@nestjs/common';
import { Request, Response } from 'express';
import { contractsZod } from '@org/contracts';
import { BffService } from './bff.service';
import { serverErrorResponseSchema, serverLinkResponseSchema } from './schemas';

@Controller('links')
export class LinksController {
  constructor(private readonly bff: BffService) {}

  @Post('invite')
  async invite(
    @Body() body: unknown,
    @Req() req: Request,
    @Res() res: Response,
  ) {
    return this.bff.forward({
      req,
      res,
      path: '/links/invite',
      method: 'POST',
      body,
      schema: serverLinkResponseSchema,
      schemasByStatus: {
        201: serverLinkResponseSchema,
        400: serverErrorResponseSchema,
        401: serverErrorResponseSchema,
        409: serverErrorResponseSchema,
      },
    });
  }

  @Post('request')
  async requestLink(
    @Body() body: unknown,
    @Req() req: Request,
    @Res() res: Response,
  ) {
    return this.bff.forward({
      req,
      res,
      path: '/links/request',
      method: 'POST',
      body,
      schema: serverLinkResponseSchema,
      schemasByStatus: {
        201: serverLinkResponseSchema,
        400: serverErrorResponseSchema,
        401: serverErrorResponseSchema,
        409: serverErrorResponseSchema,
      },
    });
  }

  @Post(':id/approve')
  async approve(
    @Param('id') id: string,
    @Req() req: Request,
    @Res() res: Response,
  ) {
    return this.bff.forward({
      req,
      res,
      path: `/links/${id}/approve`,
      method: 'POST',
      schema: contractsZod.postLinksIdApproveResponse,
      schemasByStatus: {
        200: contractsZod.postLinksIdApproveResponse,
        400: serverErrorResponseSchema,
        401: serverErrorResponseSchema,
        404: serverErrorResponseSchema,
      },
    });
  }
}
