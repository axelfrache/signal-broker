# Signal Broker - Frontend

The web dashboard and user interface for the Signal Broker platform. It allows support agents to monitor, manage, and discuss incoming tickets in real-time.

## Tech Stack

- **Framework**: React 19
- **Language**: TypeScript 5.8
- **Build Tool**: Vite
- **Styling**: Tailwind CSS v4 & shadcn/ui (Radix UI)
- **Routing**: React Router DOM
- **Data Visualization**: Recharts

## Development Setup

### Requirements

- Node.js 20+

### Installation

```bash
npm install
```

### Running Locally

To start the development server with Hot Module Replacement (HMR):

```bash
npm run dev
```

The application will be accessible at `http://localhost:5173`. Make sure the backend API is running locally to fetch data.

## Building for Production

To create an optimized production build:

```bash
npm run build
```

You can then preview the built bundle:

```bash
npm run preview
```