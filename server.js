const http = require('http');
const fs = require('fs');
const path = require('path');

const PORT = 3000;
const PUBLIC_DIR = path.join(__dirname, 'app', 'src', 'main', 'assets');

const MIME_TYPES = {
    '.html': 'text/html; charset=utf-8',
    '.css': 'text/css',
    '.js': 'text/javascript',
    '.json': 'application/json',
    '.png': 'image/png',
    '.jpg': 'image/jpeg',
    '.jpeg': 'image/jpeg',
    '.gif': 'image/gif',
    '.svg': 'image/svg+xml',
    '.ico': 'image/x-icon'
};

const server = http.createServer((req, res) => {
    // Prevent directory traversal
    let safePath = req.url.replace(/^(\.\.[\/\\])+/, '');
    // Remove query string
    safePath = safePath.split('?')[0];
    
    let filePath = path.join(PUBLIC_DIR, safePath);
    
    fs.stat(filePath, (err, stats) => {
        if (!err && stats.isDirectory()) {
            filePath = path.join(filePath, 'index.html');
        }
        
        fs.readFile(filePath, (error, content) => {
            if (error) {
                if (error.code === 'ENOENT') {
                    // Try to serve from root if not found in assets as a fallback
                    const rootFallbackPath = path.join(__dirname, safePath === '/' ? 'index.html' : safePath);
                    fs.readFile(rootFallbackPath, (fallbackError, fallbackContent) => {
                        if (fallbackError) {
                            res.writeHead(404, { 'Content-Type': 'text/plain; charset=utf-8' });
                            res.end('404 Not Found / الصفحة غير موجودة');
                        } else {
                            const ext = path.extname(rootFallbackPath).toLowerCase();
                            const contentType = MIME_TYPES[ext] || 'application/octet-stream';
                            res.writeHead(200, { 'Content-Type': contentType });
                            res.end(fallbackContent, 'utf-8');
                        }
                    });
                } else {
                    res.writeHead(500, { 'Content-Type': 'text/plain; charset=utf-8' });
                    res.end(`خطأ في الخادم: ${error.code}`);
                }
            } else {
                const ext = path.extname(filePath).toLowerCase();
                const contentType = MIME_TYPES[ext] || 'application/octet-stream';
                res.writeHead(200, { 'Content-Type': contentType });
                res.end(content, 'utf-8');
            }
        });
    });
});

server.listen(PORT, '0.0.0.0', () => {
    console.log(`Server is running at http://0.0.0.0:${PORT}/`);
});
