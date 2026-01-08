import { Body, Controller, Get, Post, Req, Res } from '@nestjs/common';
import { Request, Response } from 'express';
import { BffService } from './bff.service';
import {
  serverErrorResponseSchema,
  serverPracticeCreateResponseSchema,
  serverPracticeResponseSchema,
} from './schemas';

@Controller('practice')
export class PracticeController {
  constructor(private readonly bff: BffService) {}

  @Get()
  async getPractice(@Req() req: Request, @Res() res: Response) {
    return this.bff.forward({
      req,
      res,
      path: '/practice',
      method: 'GET',
      schema: serverPracticeResponseSchema,
      schemasByStatus: {
        200: serverPracticeResponseSchema,
      },
    });
  }

  @Post()
  async create(
    @Body() body: unknown,
    @Req() req: Request,
    @Res() res: Response,
  ) {
    return this.bff.forward({
      req,
      res,
      path: '/practice',
      method: 'POST',
      body,
      schema: serverPracticeCreateResponseSchema,
      schemasByStatus: {
        201: serverPracticeCreateResponseSchema,
        400: serverErrorResponseSchema,
        401: serverErrorResponseSchema,
      },
    });
  }
}
