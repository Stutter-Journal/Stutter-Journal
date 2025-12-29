import express from 'express';
import { betterAuth } from 'better-auth';
import { toNodeHandler } from 'better-auth/node';
import { ProductsService } from '@org/api/products';
import {
  ApiResponse,
  PaginatedResponse,
  Product,
  ProductFilter,
} from '@org/models';
import { memoryAdapter, MemoryDB } from 'better-auth/adapters/memory';

const host = process.env.HOST ?? 'localhost';
const port = process.env.PORT ? Number(process.env.PORT) : 3333;

const app = express();
const productsService = new ProductsService();
const authDb: MemoryDB = {};
const authSecret =
  process.env.BETTER_AUTH_SECRET ??
  'dev-eloquia-auth-secret-please-change-in-production';
const auth = betterAuth({
  appName: 'Eloquia Portal',
  baseURL: process.env.BETTER_AUTH_URL ?? `http://${host}:${port}`,
  basePath: '/api/auth',
  secret: authSecret,
  emailAndPassword: {
    enabled: true,
  },
  database: memoryAdapter(authDb),
});
const authHandler = toNodeHandler(auth);

// CORS configuration for Angular app
app.use((req, res, next) => {
  res.header('Access-Control-Allow-Origin', '*');
  res.header('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
  res.header(
    'Access-Control-Allow-Headers',
    'Origin, X-Requested-With, Content-Type, Accept',
  );
  if (req.method === 'OPTIONS') {
    res.sendStatus(200);
  } else {
    next();
  }
});

app.all('/api/auth', (req, res, next) => {
  authHandler(req, res).catch(next);
});

app.all('/api/auth/*', (req, res, next) => {
  authHandler(req, res).catch(next);
});

// Middleware
app.use(express.json());

app.get('/', (req, res) => {
  res.send({ message: 'Hello API' });
});

// Products endpoints
app.get('/api/products', (req, res) => {
  try {
    const filter: ProductFilter = {};

    if (req.query.category) {
      filter.category = req.query.category as string;
    }
    if (req.query.minPrice) {
      filter.minPrice = Number(req.query.minPrice);
    }
    if (req.query.maxPrice) {
      filter.maxPrice = Number(req.query.maxPrice);
    }
    if (req.query.inStock !== undefined) {
      filter.inStock = req.query.inStock === 'true';
    }
    if (req.query.searchTerm) {
      filter.searchTerm = req.query.searchTerm as string;
    }

    const page = req.query.page ? Number(req.query.page) : 1;
    const pageSize = req.query.pageSize ? Number(req.query.pageSize) : 12;

    const result = productsService.getAllProducts(filter, page, pageSize);

    const response: ApiResponse<PaginatedResponse<Product>> = {
      data: result,
      success: true,
    };

    res.json(response);
  } catch (error) {
    const response: ApiResponse<null> = {
      data: null,
      success: false,
      error: error instanceof Error ? error.message : 'Unknown error',
    };
    res.status(500).json(response);
  }
});

app.get('/api/products/:id', (req, res) => {
  try {
    const product = productsService.getProductById(req.params.id);

    if (!product) {
      const response: ApiResponse<null> = {
        data: null,
        success: false,
        error: 'Product not found',
      };
      return res.status(404).json(response);
    }

    const response: ApiResponse<Product> = {
      data: product,
      success: true,
    };

    res.json(response);
  } catch (error) {
    const response: ApiResponse<null> = {
      data: null,
      success: false,
      error: error instanceof Error ? error.message : 'Unknown error',
    };
    res.status(500).json(response);
  }
});

app.get('/api/products-metadata/categories', (req, res) => {
  try {
    const categories = productsService.getCategories();
    const response: ApiResponse<string[]> = {
      data: categories,
      success: true,
    };
    res.json(response);
  } catch (error) {
    const response: ApiResponse<null> = {
      data: null,
      success: false,
      error: error instanceof Error ? error.message : 'Unknown error',
    };
    res.status(500).json(response);
  }
});

app.get('/api/products-metadata/price-range', (req, res) => {
  try {
    const priceRange = productsService.getPriceRange();
    const response: ApiResponse<{ min: number; max: number }> = {
      data: priceRange,
      success: true,
    };
    res.json(response);
  } catch (error) {
    const response: ApiResponse<null> = {
      data: null,
      success: false,
      error: error instanceof Error ? error.message : 'Unknown error',
    };
    res.status(500).json(response);
  }
});

app.listen(port, host, () => {
  console.log(`[ ready ] http://${host}:${port}`);
});
