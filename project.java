import processing.serial.*;

// ---------------- SERIAL ----------------
Serial myPort;

// ---------------- DATA ----------------
String data;
int iAngle = 0;
int iDistance = 0;
float temperature = 0;

// ---------------- FILTERING ----------------
float smoothDistance = 0;
float prevDistance = 0;
float speed = 0;

// ---------------- TARGET ----------------
String targetType = "NONE";
String thermalStatus = "COLD";
String threatLevel = "LOW";

color targetColor;
color threatColor;

// missile confirmation
int missileCounter = 0;

// ---------------- SETUP ----------------
void setup() {
  size(1366, 768);
  smooth();

  myPort = new Serial(this, "COM5", 9600);
  myPort.bufferUntil('.');
}

// ---------------- DRAW ----------------
void draw() {

  fill(0, 40);
  noStroke();
  rect(0, 0, width, height);

  drawRadar();
  drawSweep();
  drawTarget();
  drawText();
}

// ---------------- SERIAL EVENT ----------------
void serialEvent(Serial myPort) {

  data = myPort.readStringUntil('.');
  if (data == null) return;

  data = trim(data);
  data = data.substring(0, data.length() - 1);

  String[] values = split(data, ',');
  if (values.length < 3) return;

  iAngle = int(values[0]);
  iDistance = int(values[1]);
  temperature = float(values[2]);

  smoothDistance = lerp(smoothDistance, iDistance, 0.3);

  speed = abs(smoothDistance - prevDistance);
  prevDistance = smoothDistance;

  classifyTarget();
  detectThermal();
  assignThreat();
}

// ---------------- CLASSIFY TARGET ----------------
void classifyTarget() {

  if (speed > 6 && smoothDistance > 25) {
    missileCounter++;
  } else {
    missileCounter = 0;
  }

  if (missileCounter > 5) {
    targetType = "MISSILE";
  }
  else if (speed > 2.5) {
    targetType = "DRONE";
  }
  else {
    targetType = "BIRD";
  }
}

// ---------------- THERMAL DETECTION ----------------
void detectThermal() {
  if (temperature > 45) {
    thermalStatus = "HOT";
  } else {
    thermalStatus = "COLD";
  }
}

// ---------------- THREAT LEVEL ----------------
void assignThreat() {

  if (targetType.equals("MISSILE") && thermalStatus.equals("HOT")) {
    threatLevel = "HIGH";
    threatColor = color(255, 0, 0);
  }
  else if (targetType.equals("DRONE") && thermalStatus.equals("HOT")) {
    threatLevel = "MEDIUM";
    threatColor = color(255, 165, 0);
  }
  else {
    threatLevel = "LOW";
    threatColor = color(0, 255, 0);
  }

  targetColor = threatColor;
}

// ---------------- RADAR ----------------
void drawRadar() {

  pushMatrix();
  translate(width / 2, height * 0.92);

  stroke(0, 255, 0);
  strokeWeight(2);
  noFill();

  for (int i = 1; i <= 4; i++) {
    arc(0, 0, width * i * 0.25, width * i * 0.25, PI, TWO_PI);
  }

  for (int a = 30; a <= 150; a += 30) {
    line(0, 0,
         (width / 2) * cos(radians(a)),
        -(width / 2) * sin(radians(a)));
  }

  line(-width / 2, 0, width / 2, 0);
  popMatrix();
}

// ---------------- SWEEP ----------------
void drawSweep() {

  pushMatrix();
  translate(width / 2, height * 0.92);

  stroke(0, 255, 0);
  strokeWeight(4);

  line(0, 0,
       (height * 0.85) * cos(radians(iAngle)),
      -(height * 0.85) * sin(radians(iAngle)));

  popMatrix();
}

// ---------------- DRAW TARGET ----------------
void drawTarget() {

  if (smoothDistance > 40) return;

  pushMatrix();
  translate(width / 2, height * 0.92);

  float r = smoothDistance * 12;
  float x = r * cos(radians(iAngle));
  float y = -r * sin(radians(iAngle));

  stroke(targetColor);
  fill(targetColor);
  strokeWeight(6);

  if (targetType.equals("MISSILE")) {
    triangle(x, y, x - 6, y + 12, x + 6, y + 12);
  }
  else if (targetType.equals("DRONE")) {
    ellipse(x, y, 14, 14);
  }
  else {
    point(x, y);
  }

  popMatrix();
}

// ---------------- TEXT ----------------
void drawText() {

  fill(0);
  rect(0, height - 60, width, 60);

  fill(0, 255, 0);
  textSize(22);

  text("TARGET : " + targetType, 30, height - 25);
  text("ANGLE : " + iAngle + "°", 230, height - 25);
  text("DISTANCE : " + int(smoothDistance) + " cm", 420, height - 25);
  text("TEMP : " + temperature + " °C", 660, height - 25);
  text("THERMAL : " + thermalStatus, 850, height - 25);

  fill(threatColor);
  textSize(26);
  text("THREAT LEVEL : " + threatLevel, width - 380, height - 25);
}
