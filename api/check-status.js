import https from 'https';

const KV_BUCKET = '6iH8JqG7j4Zk8P2vNwKx1y'; // Persistent cloud bucket

function getFromCloudKV(key) {
  return new Promise((resolve) => {
    try {
      const req = https.request({
        hostname: 'kvdb.io',
        port: 443,
        path: `/${KV_BUCKET}/${encodeURIComponent(key)}`,
        method: 'GET',
        headers: {
          'Accept': 'application/json'
        },
        timeout: 3000
      }, (res) => {
        let raw = '';
        res.on('data', (chunk) => { raw += chunk; });
        res.on('end', () => {
          if (res.statusCode >= 200 && res.statusCode < 300 && raw) {
            try {
              const json = JSON.parse(raw);
              resolve(json.status || raw.trim());
            } catch (e) {
              resolve(raw.trim());
            }
          } else {
            resolve(null);
          }
        });
      });
      req.on('error', () => resolve(null));
      req.on('timeout', () => { req.destroy(); resolve(null); });
      req.end();
    } catch (e) {
      resolve(null);
    }
  });
}

global.approvalStore = global.approvalStore || {};

export default async function handler(req, res) {
  res.setHeader('Access-Control-Allow-Credentials', 'true');
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET,OPTIONS,PATCH,DELETE,POST,PUT');
  res.setHeader('Cache-Control', 'no-cache, no-store, must-revalidate');
  res.setHeader('Pragma', 'no-cache');

  if (req.method === 'OPTIONS') {
    return res.status(200).end();
  }

  const { username } = req.query;

  if (!username) {
    return res.status(400).json({ status: "ERROR", message: "Username parameter is required" });
  }

  const cleanUser = username.trim().toLowerCase();

  // 1. Check in-memory store
  let currentStatus = global.approvalStore[cleanUser];

  // 2. Check persistent cloud KV store
  if (!currentStatus || currentStatus === 'PENDING') {
    const cloudStatus = await getFromCloudKV(cleanUser);
    if (cloudStatus) {
      currentStatus = cloudStatus;
      global.approvalStore[cleanUser] = cloudStatus;
    }
  }

  const finalStatus = currentStatus || 'PENDING';

  return res.status(200).json({
    username: username,
    status: finalStatus,
    timestamp: Date.now()
  });
}
