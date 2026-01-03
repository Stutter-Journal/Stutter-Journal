import { Body, Controller, Param, Post, Req, Res } from '@nestjs/common';
import { Request, Response } from 'express';
import { BffService } from './bff.service';
import {
  serverLinkApproveResponseSchema,
  serverLinkResponseSchema,
} from './schemas';

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
      schema: serverLinkApproveResponseSchema,
    });
  }
}
