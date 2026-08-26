// Serverless Handler with Persistent Cloud Multi-Channel Sync
import https from 'https';

const KV_BUCKET = '6iH8JqG7j4Zk8P2vNwKx1y';

function notifyNtfy(key, value) {
  return new Promise((resolve) => {
    try {
      const topic = `pwsara_auth_${key.replace(/[^a-z0-9]/gi, '_')}`;
      const postData = value;
      const req = https.request({
        hostname: 'ntfy.sh',
        port: 443,
        path: `/${topic}`,
        method: 'POST',
        headers: {
          'Title': `PW SARA Authorization - ${key}`,
          'Content-Type': 'text/plain',
          'Content-Length': Buffer.byteLength(postData)
        },
        timeout: 3000
      }, (res) => {
        resolve(res.statusCode >= 200 && res.statusCode < 300);
      });
      req.on('error', () => resolve(false));
      req.on('timeout', () => { req.destroy(); resolve(false); });
      req.write(postData);
      req.end();
    } catch (e) {
      resolve(false);
    }
  });
}

function saveToCloudKV(key, value) {
  return new Promise((resolve) => {
    try {
      const data = JSON.stringify({ status: value, timestamp: Date.now() });
      const req = https.request({
        hostname: 'kvdb.io',
        port: 443,
        path: `/${KV_BUCKET}/${encodeURIComponent(key)}`,
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Content-Length': Buffer.byteLength(data)
        },
        timeout: 3000
      }, (res) => {
        resolve(res.statusCode >= 200 && res.statusCode < 300);
      });
      req.on('error', () => resolve(false));
      req.on('timeout', () => { req.destroy(); resolve(false); });
      req.write(data);
      req.end();
    } catch (e) {
      resolve(false);
    }
  });
}

global.approvalStore = global.approvalStore || {};

export default async function handler(req, res) {
  res.setHeader('Access-Control-Allow-Credentials', 'true');
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET,OPTIONS,PATCH,DELETE,POST,PUT');
  res.setHeader('Cache-Control', 'no-cache, no-store, must-revalidate');

  if (req.method === 'OPTIONS') {
    return res.status(200).end();
  }

  const { action, username } = req.query;

  if (!username || !action) {
    return res.status(400).send("Missing username or action parameter.");
  }

  const cleanUser = username.trim().toLowerCase();
  const cleanAction = action.trim().toUpperCase();
  const targetStatus = cleanAction === 'APPROVE' ? 'APPROVED' : 'DENIED';

  // 1. Save to in-memory store
  global.approvalStore[cleanUser] = targetStatus;

  // 2. Broadcast to ntfy real-time topic & cloud KV
  await Promise.all([
    notifyNtfy(cleanUser, targetStatus),
    saveToCloudKV(cleanUser, targetStatus)
  ]);

  const isApproved = targetStatus === 'APPROVED';
  const color = isApproved ? '#22c55e' : '#ef4444';
  const title = isApproved ? '✔ Request Approved' : '✖ Request Denied';
  const actionText = isApproved ? 'authorized' : 'blocked / denied';

  res.setHeader('Content-Type', 'text/html; charset=utf-8');
  return res.status(200).send(`
    <!DOCTYPE html>
    <html lang="en">
    <head>
      <meta charset="UTF-8">
      <meta name="viewport" content="width=device-width, initial-scale=1.0">
      <title>PW SARA Admin - ${cleanAction}</title>
      <style>
        body {
          background-color: #0b0c10;
          color: #ffffff;
          font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
          display: flex;
          justify-content: center;
          align-items: center;
          min-height: 100vh;
          margin: 0;
          padding: 20px;
          box-sizing: border-box;
        }
        .card {
          background-color: #161922;
          border: 1px solid #232734;
          border-radius: 16px;
          padding: 40px 32px;
          max-width: 440px;
          width: 100%;
          text-align: center;
          box-shadow: 0 20px 40px rgba(0,0,0,0.6);
        }
        .badge {
          display: inline-block;
          font-size: 13px;
          font-weight: 700;
          letter-spacing: 0.5px;
          padding: 6px 16px;
          border-radius: 20px;
          background: ${isApproved ? 'rgba(34, 197, 94, 0.15)' : 'rgba(239, 68, 68, 0.15)'};
          color: ${color};
          border: 1px solid ${color}44;
          margin-bottom: 20px;
        }
        h1 { color: ${color}; font-size: 26px; margin: 0 0 14px; font-weight: 800; }
        p { color: #d1d5db; font-size: 16px; line-height: 1.6; margin: 0 0 16px; }
        .details {
          background: #0f1117;
          border: 1px solid #1f2430;
          border-radius: 10px;
          padding: 14px;
          margin: 20px 0;
          font-family: monospace;
          font-size: 14px;
          color: #93c5fd;
        }
        .sub { color: #9ca3af; font-size: 13px; margin-top: 24px; border-top: 1px solid #1f2430; padding-top: 16px; }
      </style>
    </head>
    <body>
      <div class="card">
        <div class="badge">${cleanAction} PROCESSED</div>
        <h1>${title}</h1>
        <p>Student has been successfully <b>${actionText}</b>.</p>
        
        <div class="details">
          User: <strong>${username}</strong><br>
          Status: <strong>${targetStatus}</strong>
        </div>

        <p class="sub">
          ⚡ The student's Android app will automatically unlock in the next 1–2 seconds.
        </p>
      </div>
    </body>
    </html>
  `);
}
