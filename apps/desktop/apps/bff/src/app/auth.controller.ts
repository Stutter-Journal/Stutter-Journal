import { Body, Controller, Get, Post, Req, Res } from '@nestjs/common';
import { Request, Response } from 'express';
import { BffService } from './bff.service';
import {
  serverDoctorMeResponseSchema,
  serverDoctorResponseSchema,
  serverStatusResponseSchema,
} from './schemas';

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
      schema: serverDoctorMeResponseSchema,
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
      schema: serverDoctorResponseSchema,
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
      schema: serverDoctorResponseSchema,
    });
  }

  @Post('logout')
  async logout(@Req() req: Request, @Res() res: Response) {
    return this.bff.forward({
      req,
      res,
      path: '/doctor/logout',
      method: 'POST',
      schema: serverStatusResponseSchema,
    });
  }
}
