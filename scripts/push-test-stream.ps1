ffmpeg -re -f lavfi -i "testsrc=size=640x480:rate=25" `
  -f lavfi -i "sine=frequency=1000" `
  -c:v libx264 -preset veryfast -c:a aac `
  -rtsp_transport tcp `
  -f rtsp rtsp://localhost:8554/test
