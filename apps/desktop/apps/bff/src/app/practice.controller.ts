import { Body, Controller, Get, Post, Req, Res } from '@nestjs/common';
import { Request, Response } from 'express';
import { BffService } from './bff.service';
import { schemas } from '@org/contracts';

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
      schema: schemas.serverPracticeResponseSchema,
      schemasByStatus: {
        200: schemas.serverPracticeResponseSchema,
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
      schema: schemas.serverPracticeCreateResponseSchema,
      schemasByStatus: {
        201: schemas.serverPracticeCreateResponseSchema,
        400: schemas.serverErrorResponseSchema,
        401: schemas.serverErrorResponseSchema,
      },
    });
  }
}
