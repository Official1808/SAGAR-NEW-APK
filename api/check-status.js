global.approvalStore = global.approvalStore || {};

export default function handler(req, res) {
  // Support CORS preflight
  if (req.method === 'OPTIONS') {
    return res.status(200).end();
  }

  const { username } = req.query;

  if (!username) {
    return res.status(400).json({ status: "ERROR", message: "Username parameter is required" });
  }

  const cleanUser = username.trim().toLowerCase();
  const currentStatus = global.approvalStore[cleanUser] || "PENDING";

  return res.status(200).json({
    username: username,
    status: currentStatus // "PENDING", "APPROVED", or "DENIED"
  });
}
