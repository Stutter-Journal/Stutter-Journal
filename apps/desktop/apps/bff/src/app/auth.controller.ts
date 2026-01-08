import { Body, Controller, Get, Post, Req, Res } from '@nestjs/common';
import { Request, Response } from 'express';
import { contractsZod } from '@eloquia/shared/api/contracts';
import { BffService } from './bff.service';
import { serverErrorResponseSchema } from './schemas';

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
        401: serverErrorResponseSchema,
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
        400: serverErrorResponseSchema,
        401: serverErrorResponseSchema,
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
        400: serverErrorResponseSchema,
        409: serverErrorResponseSchema,
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
        401: serverErrorResponseSchema,
      },
    });
  }
}
