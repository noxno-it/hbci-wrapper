# HBCI Frontend

Angular 20 frontend application for the HBCI Wrapper microservice.

## Features

- Dashboard with overview of banking operations
- Bank account management
- Transaction viewing
- Balance checking
- Responsive design

## Prerequisites

- Node.js 18+ and npm
- Angular CLI 20
- HBCI Backend service running on `http://localhost:8080`

## Installation

```bash
cd hbci-frontend
npm install
```

## Development Server

Start the development server:

```bash
npm start
```

Navigate to `http://localhost:4200/`. The application will automatically reload if you change any of the source files. The proxy configuration will forward API requests to the backend service.

## Build

Build the project for production:

```bash
npm run build
```

The build artifacts will be stored in the `dist/` directory.

## Testing

Run unit tests:

```bash
npm test
```

## Project Structure

```
src/
├── app/
│   ├── accounts/          # Account management components
│   ├── dashboard/         # Dashboard component
│   ├── services/          # API services
│   ├── app.component.ts   # Root component
│   └── app.routes.ts      # Application routes
├── assets/                # Static assets
├── index.html            # Main HTML file
├── main.ts               # Application entry point
└── styles.scss           # Global styles
```

## API Integration

The frontend communicates with the backend via the `HbciService` which wraps HTTP calls to:
- `/api/accounts` - Bank account management
- `/api/hbci/balance/{id}` - Balance retrieval
- `/api/hbci/transactions/*` - Transaction operations

## Configuration

The proxy configuration in `proxy.conf.json` routes `/api/*` requests to `http://localhost:8080`. Update this file for different backend URLs.

## Next Steps

- Implement transaction detail view
- Add balance display component
- Implement account creation form
- Add authentication/authorization
- Enhance error handling
- Add loading indicators
- Implement pagination for transactions

## Contributing

This is part of the HBCI Wrapper microservice project. See the main [README](../README.md) for more information.
