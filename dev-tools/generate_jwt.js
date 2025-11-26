#!/usr/bin/env node
// Simple dev JWT generator (HS256) for local testing only.
// Usage: node generate_jwt.js '{"sub":"sysadmin","roles":["SYSTEM_ADMIN"],"tenant_schema":"acme_testnext"}' mysecret

const jwt = require('jsonwebtoken');

const args = process.argv.slice(2);
if (args.length < 2) {
  console.error('Usage: node generate_jwt.js <claims-json> <secret> [expiresIn]');
  process.exit(2);
}

const claims = JSON.parse(args[0]);
const secret = args[1];
const expiresIn = args[2] || '1h';

const token = jwt.sign(claims, secret, { algorithm: 'HS256', expiresIn });
console.log(token);
