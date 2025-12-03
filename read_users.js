const fs = require('fs');
try {
    const data = fs.readFileSync('users_clean.json', 'utf8');
    // Find the JSON part (skip curl output)
    const jsonStart = data.indexOf('[');
    if (jsonStart === -1) {
        console.log("No JSON found");
        process.exit(1);
    }
    const jsonStr = data.substring(jsonStart);
    const users = JSON.parse(jsonStr);
    console.log("Users found:");
    users.forEach(u => console.log(`Username: '${u.username}', Role: '${u.role}', ID: '${u.id}'`));
} catch (e) {
    console.error(e);
}
