const http = require('http');

const USERNAME = 'SysAdmin';
const PASSWORD = 'neeraj.maurya@testnext.com';
const BASE_URL = 'http://localhost:8080';

// Helper to make HTTP requests
function request(method, path, body = null, token = null, isApiKey = false) {
    return new Promise((resolve, reject) => {
        const options = {
            hostname: 'localhost',
            port: 8080,
            path: path,
            method: method,
            headers: {
                'Content-Type': 'application/json',
            }
        };

        if (token) {
            if (isApiKey) {
                options.headers['x-api-key'] = token;
            } else {
                // Basic Auth
                options.headers['Authorization'] = 'Basic ' + Buffer.from(token).toString('base64');
            }
        }

        const req = http.request(options, (res) => {
            let data = '';
            res.on('data', (chunk) => data += chunk);
            res.on('end', () => {
                resolve({
                    statusCode: res.statusCode,
                    headers: res.headers,
                    body: data ? JSON.parse(data) : {}
                });
            });
        });

        req.on('error', (e) => reject(e));

        if (body) {
            req.write(JSON.stringify(body));
        }
        req.end();
    });
}

async function runSanityCheck() {
    console.log(`Starting Backend Sanity Check for user: ${USERNAME}`);
    console.log('--------------------------------------------------');

    try {
        // 1. Login (Basic Auth) -> Get Profile & API Key
        console.log('[1] Testing Login (/api/system/users/me)...');
        const basicAuthCreds = `${USERNAME}:${PASSWORD}`;
        const loginRes = await request('GET', '/api/system/users/me', null, basicAuthCreds, false);

        if (loginRes.statusCode !== 200) {
            console.error(`[FAILED] Login failed with status ${loginRes.statusCode}`);
            console.error('Response:', JSON.stringify(loginRes.body, null, 2));
            process.exit(1);
        }

        const userProfile = loginRes.body;
        console.log(`[SUCCESS] Logged in as ${userProfile.username} (${userProfile.role})`);

        const apiKey = userProfile.api_key; // Note: key might be snake_case from DB
        if (!apiKey) {
            console.warn('[WARNING] No API Key found in profile. Hybrid auth test might fail if enforced.');
        } else {
            console.log(`[INFO] Retrieved API Key: ${apiKey.substring(0, 5)}...`);
        }

        // Use API Key if available, otherwise fall back to Basic Auth for rest of tests
        // User wants Hybrid approach, so let's try to use API Key if we have it.
        const authToken = apiKey || basicAuthCreds;
        const isApiKey = !!apiKey;
        console.log(`[INFO] Proceeding with ${isApiKey ? 'API Key' : 'Basic Auth'} for subsequent requests.`);

        // 2. List System Users
        console.log('\n[2] Testing List Users (/api/system/users)...');
        const listRes = await request('GET', '/api/system/users', null, authToken, isApiKey);
        if (listRes.statusCode !== 200) {
            console.error(`[FAILED] List Users failed with status ${listRes.statusCode}`);
            process.exit(1);
        }
        console.log(`[SUCCESS] Retrieved ${listRes.body.length} users.`);

        // 3. Create Tenant
        console.log('\n[3] Testing Create Tenant (/api/tenants)...');
        const tenantName = `SanityTenant_${Date.now()}`;
        const tenantBody = {
            name: tenantName,
            schemaName: tenantName.toLowerCase(),
            testManagerId: userProfile.id // Assigning self as manager for simplicity
        };
        const tenantRes = await request('POST', '/api/tenants', tenantBody, authToken, isApiKey);
        if (tenantRes.statusCode !== 200) {
            console.error(`[FAILED] Create Tenant failed with status ${tenantRes.statusCode}`);
            console.error('Response:', JSON.stringify(tenantRes.body, null, 2));
            process.exit(1);
        }
        const tenant = tenantRes.body;
        console.log(`[SUCCESS] Created Tenant: ${tenant.name} (ID: ${tenant.id})`);

        // 4. Create Project
        console.log(`\n[4] Testing Create Project (/api/tenants/${tenant.id}/projects)...`);
        const projectBody = {
            name: `SanityProject_${Date.now()}`,
            description: "Created by Sanity Check"
        };
        const projectRes = await request('POST', `/api/tenants/${tenant.id}/projects`, projectBody, authToken, isApiKey);
        if (projectRes.statusCode !== 200) {
            console.error(`[FAILED] Create Project failed with status ${projectRes.statusCode}`);
            console.error('Response:', JSON.stringify(projectRes.body, null, 2));
            process.exit(1);
        }
        const project = projectRes.body;
        console.log(`[SUCCESS] Created Project: ${project.name} (ID: ${project.id})`);

        // 5. Create Test Suite
        console.log(`\n[5] Testing Create Test Suite (/api/projects/${project.id}/suites)...`);
        const suiteBody = {
            name: "Sanity Suite",
            description: "Smoke test suite"
        };
        const suiteRes = await request('POST', `/api/projects/${project.id}/suites`, suiteBody, authToken, isApiKey);
        if (suiteRes.statusCode !== 200) {
            console.error(`[FAILED] Create Suite failed with status ${suiteRes.statusCode}`);
            console.error('Response:', JSON.stringify(suiteRes.body, null, 2));
            process.exit(1);
        }
        const suite = suiteRes.body;
        console.log(`[SUCCESS] Created Suite: ${suite.name} (ID: ${suite.id})`);

        console.log('\n--------------------------------------------------');
        console.log('SANITY CHECK PASSED: All backend APIs are functional.');

    } catch (e) {
        console.error('\n[ERROR] Sanity check failed with exception:', e);
        process.exit(1);
    }
}

runSanityCheck();
