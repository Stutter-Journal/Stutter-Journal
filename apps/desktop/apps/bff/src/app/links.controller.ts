import { Body, Controller, Param, Post, Req, Res } from '@nestjs/common';
import { Request, Response } from 'express';
import { BffService } from './bff.service';
import { contractsZod, schemas } from '@org/contracts';

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
      schema: schemas.serverLinkResponseSchema,
      schemasByStatus: {
        201: schemas.serverLinkResponseSchema,
        400: schemas.serverErrorResponseSchema,
        401: schemas.serverErrorResponseSchema,
        409: schemas.serverErrorResponseSchema,
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
      schema: schemas.serverLinkResponseSchema,
      schemasByStatus: {
        201: schemas.serverLinkResponseSchema,
        400: schemas.serverErrorResponseSchema,
        401: schemas.serverErrorResponseSchema,
        409: schemas.serverErrorResponseSchema,
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
        400: schemas.serverErrorResponseSchema,
        401: schemas.serverErrorResponseSchema,
        404: schemas.serverErrorResponseSchema,
      },
    });
  }

  @Post('pairing-code')
  async createPairingCode(@Req() req: Request, @Res() res: Response) {
    return this.bff.forward({
      req,
      res,
      path: '/links/pairing-code',
      method: 'POST',
      schema: schemas.serverPairingCodeCreateResponseSchema,
      schemasByStatus: {
        201: schemas.serverPairingCodeCreateResponseSchema,
        401: schemas.serverErrorResponseSchema,
        500: schemas.serverErrorResponseSchema,
      },
    });
  }

  @Post('pairing-code/redeem')
  async redeemPairingCode(
    @Body() body: unknown,
    @Req() req: Request,
    @Res() res: Response,
  ) {
    return this.bff.forward({
      req,
      res,
      path: '/links/pairing-code/redeem',
      method: 'POST',
      body,
      schema: contractsZod.postLinksPairingCodeRedeemResponse,
      schemasByStatus: {
        200: contractsZod.postLinksPairingCodeRedeemResponse,
        400: schemas.serverErrorResponseSchema,
        401: schemas.serverErrorResponseSchema,
        404: schemas.serverErrorResponseSchema,
        409: schemas.serverErrorResponseSchema,
        500: schemas.serverErrorResponseSchema,
      },
    });
  }
}
