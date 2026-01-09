import { Body, Controller, Get, Post, Req, Res } from '@nestjs/common';
import { Request, Response } from 'express';
import { contractsZod, schemas } from '@org/contracts';
import { BffService } from './bff.service';

@Controller('doctor')
export class AuthController {
  constructor(private readonly bff: BffService) {}

  @Get('me')
  async me(@Req() req: Request, @Res() res: Response) {
    return this.bff.forward({
      req,
      res,
      path: '/doctor/me',
      method: 'GET',
      schema: contractsZod.getDoctorMeResponse,
      schemasByStatus: {
        200: contractsZod.getDoctorMeResponse,
        401: schemas.serverErrorResponseSchema,
      },
    });
  }

  @Post('login')
  async login(
    @Body() body: unknown,
    @Req() req: Request,
    @Res() res: Response,
  ) {
    return this.bff.forward({
      req,
      res,
      path: '/doctor/login',
      method: 'POST',
      body,
      schema: contractsZod.postDoctorLoginResponse,
      schemasByStatus: {
        200: contractsZod.postDoctorLoginResponse,
        400: schemas.serverErrorResponseSchema,
        401: schemas.serverErrorResponseSchema,
      },
    });
  }

  @Post('register')
  async register(
    @Body() body: unknown,
    @Req() req: Request,
    @Res() res: Response,
  ) {
    return this.bff.forward({
      req,
      res,
      path: '/doctor/register',
      method: 'POST',
      body,
      schema: contractsZod.postDoctorLoginResponse,
      schemasByStatus: {
        201: contractsZod.postDoctorLoginResponse,
        400: schemas.serverErrorResponseSchema,
        409: schemas.serverErrorResponseSchema,
      },
    });
  }

  @Post('logout')
  async logout(@Req() req: Request, @Res() res: Response) {
    return this.bff.forward({
      req,
      res,
      path: '/doctor/logout',
      method: 'POST',
      schema: contractsZod.postDoctorLogoutResponse,
      schemasByStatus: {
        200: contractsZod.postDoctorLogoutResponse,
        401: schemas.serverErrorResponseSchema,
      },
    });
  }
}
