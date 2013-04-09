#include <iostream>
#include <cstdlib>
#include <math.h>
#include <time.h>
#include <stdlib.h>
#include <string>
#include <sstream>
#include <unordered_map>
#include <deque>
#include <unordered_set>

#include <X11/Xlib.h>
#include <X11/Xutil.h>

using namespace std;

#define PI 3.14159265
// Global Constants
const int BORDER = 5;
const int TARGET_FPS = 60;

// Helper Function definitions
static int roundTowardsZero(double);
static double timeDiff(clock_t, clock_t);

// Data Structure definitions
struct XInfo {
	Display *display;
	int screen;
	Window window;
	GC gc[3];
};

struct HitBox{
	int x1;
	int y1;
	int x2;
	int y2;
};

// Class implementations
class DrawObject {
public:
	virtual void draw(XInfo &xInfo) = 0;
};

class Missile: public DrawObject {
protected:
	int x;
	int y;
	int length;

	double xVelocity;
	double yVelocity;
	double xDelta;
	double yDelta;

	double angle;

public:
	Missile(int x, int y, double xVelocity, double yVelocity, int scrollSpeed) : x(x), y(y), length(10), xVelocity(xVelocity), yVelocity(yVelocity), xDelta(0), yDelta(0) {
		if (xVelocity - scrollSpeed == 0)
			angle = PI / 2;
		else
			angle = atan(yVelocity / (xVelocity + scrollSpeed));
	}
	~Missile() {}

	void move() {
		double intPart = 0;
		double fracPart = modf(xVelocity, &intPart);

		int d = roundTowardsZero(xDelta);
		x += (int)intPart + d;
		xDelta += fracPart - d;

		intPart = 0;
		fracPart = modf(yVelocity, &intPart);

		d = roundTowardsZero(yDelta);
		y += (int)intPart + d;
		yDelta += fracPart - d;
	}

	void draw(XInfo &xInfo) {
		XDrawLine(xInfo.display, xInfo.window, xInfo.gc[2], x, y, x + length * cos(angle), y + length * sin(angle));
	}

	void resize(int l) {
		length = l;
	}

	bool onscreen(int screenHeight, int screenWidth) {
		int x2 = x + length * cos(angle);
		int y2 = y + length * sin(angle);

		if ((x >= 0 && x <= screenWidth) || (x2 >= 0 && x2 <= screenWidth)) 
			return true;
		if ((y >= 0 && y <= screenHeight) || (y2 >= 0 && y2 <= screenHeight))
			return true;
		return false;
	}

	bool hitDetect(HitBox t) {
		int x2 = x + length * cos(angle);
		int y2 = y + length * sin(angle);

		if ((x >= t.x1 && x <= t.x2) || (x2 >= t.x1 && x2 <= t.x2)) {
			if ((y >= t.y1 && y <= t.y2) || (y2 >= t.y1 && y2 <= t.y2))
				return true;
		}	
		return false;
	}
};

class Bomb: public DrawObject {
protected:
	static constexpr double DOWNWARD_ACCELERATION = 0.1;
	int x;
	int y;
	int height;
	int width;

	double xVelocity;
	double yVelocity;
	double xDelta;
	double yDelta;

public:
	Bomb(int x, int y, double xVelocity, double yVelocity) : x(x), y(y), height(10), width(15), xVelocity(xVelocity), yVelocity(yVelocity), xDelta(0), yDelta(0) {}
	~Bomb() {}

	void move() {
		double intPart = 0;
		double fracPart = modf(xVelocity, &intPart);

		int d = roundTowardsZero(xDelta);
		x += (int)intPart + d;
		xDelta += fracPart - d;

		intPart = 0;
		fracPart = modf(yVelocity, &intPart);

		d = roundTowardsZero(yDelta);
		y += (int)intPart + d;
		yDelta += fracPart - d;

		accelerate();
	}

	void draw(XInfo &xInfo) {
		// XDrawRectangle(xInfo.display, xInfo.window, xInfo.gc[0], x, y, width, height);
		XFillArc(xInfo.display, xInfo.window, xInfo.gc[0], x, y, width, height, 0, 360*64);
	}

	HitBox getHitBox() {
		HitBox h = (HitBox) {x, y, x + width, y + height};
		return h;
	}

protected:
	void accelerate() {
		yVelocity += DOWNWARD_ACCELERATION;
		// xVelocity += xVelocity * -0.1;
	}
};

class Turret: public DrawObject {
protected:
	static constexpr double WIDTH_RATIO = 0.6;
	static constexpr double MISSILE_VELOCITY = 5.0;
	int x;
	int y;
	int height;
	int width;
	double angle;

public:
	Turret(int x, int y, int height, int width) : x(x), y(y), height(height), width(width), angle(0) {}
	~Turret() {}

	Missile* shoot(int scrollSpeed) {
		int cannonLength = width / 2;
		int baseWidth = width * WIDTH_RATIO;
		int baseHeight = height - cannonLength;
		int baseX = x + 0.5*(width - baseWidth);
		int baseY = y + height - baseHeight;
		int cannonPivotX = baseX + 0.5*baseWidth;
		int cannonPivotY = baseY;
		int cannonPointX = cannonPivotX + cannonLength*sin(angle);
		int cannonPointY = cannonPivotY - cannonLength*cos(angle);

		Missile* m = new Missile(cannonPointX, cannonPointY, MISSILE_VELOCITY * sin(angle), MISSILE_VELOCITY * cos(angle) * -1, scrollSpeed);
		return m;
	}

	bool hitDetect(HitBox t) {
		if ((t.x1 >= x && t.x1 <= x + width) || (t.x2 >= x && t.x2 <= x + width)) {
			if ((t.y1 >= y && t.y1 <= y + height) || (t.y2 >= y && t.y2 <= y + height))
				return true;
		}
		return false;
	}

	void trackChopper(int cx, int cy) {
		double dx = x - cx;
		double dy = y - cy;
		if (dy < 0) {
			angle = PI / 2 * (dx < 0 ? 1 : -1);
			return;
		}

		angle = -atan(dx / dy);
	}

	void draw(XInfo &xInfo) {
		int cannonLength = width / 2;
		int baseWidth = width * WIDTH_RATIO;
		int baseHeight = height - cannonLength;

		// Bounding box
		// XDrawRectangle(xInfo.display, xInfo.window, xInfo.gc[0], x, y, width, height);

		// Base
		int baseX = x + 0.5*(width - baseWidth);
		int baseY = y + height - baseHeight;
		XFillRectangle(xInfo.display, xInfo.window, xInfo.gc[0], baseX, baseY, baseWidth, baseHeight);
		XFillArc(xInfo.display, xInfo.window, xInfo.gc[0], baseX, baseY - baseWidth*0.5, baseWidth, baseWidth, 0, 180*64);

		// Cannon
		int cannonPivotX = baseX + 0.5*baseWidth;
		int cannonPivotY = baseY;

		int cannonPointX = cannonPivotX + cannonLength*sin(angle);
		int cannonPointY = cannonPivotY - cannonLength*cos(angle);

		// XDrawLine(xInfo.display, xInfo.window, xInfo.gc[1], cannonPivotX - 2, cannonPivotY, cannonPointX - 2, cannonPointY);
		// XDrawLine(xInfo.display, xInfo.window, xInfo.gc[1], cannonPivotX + 2, cannonPivotY, cannonPointX + 2, cannonPointY);

		XDrawLine(xInfo.display, xInfo.window, xInfo.gc[2], cannonPivotX, cannonPivotY, cannonPointX, cannonPointY);
	}

	void incrementX(int amount) {
		x -= amount;
	}

	void resize(int newX, int newY, int newHeight, int newWidth) {
		x = newX;
		y = newY;
		height = newHeight;
		width = newWidth;
	}

	HitBox getHitBox() {
		HitBox h = (HitBox) {x, y, x + width, y + height};
		return h;
	}
};

class Building: public DrawObject {
protected:
	static const int WIDTH_RATIO = 10;

	int width;
	int height;
	int x;
	int y;

	unsigned int screenHeight;
	unsigned int screenWidth;

	double heightPercent;

	Turret* turret;

public:
	Building(int screenHeight, int screenWidth, double heightPercent, int x, int y, bool hasTurret) : screenHeight(screenHeight), screenWidth(screenWidth), heightPercent(heightPercent), x(x), y(y) {
		width = screenWidth / WIDTH_RATIO;
		height = screenHeight * heightPercent;
		if (hasTurret) 
			initTurret();
		else
			turret = NULL;
	};
	Building(int screenHeight, int screenWidth, double heightPercent, bool hasTurret) : screenHeight(screenHeight), screenWidth(screenWidth), heightPercent(heightPercent) {
		x = screenWidth;
		width = screenWidth / WIDTH_RATIO;
		height = screenHeight * heightPercent;
		y = screenHeight - height;
		if (hasTurret)
			initTurret();
		else
			turret = NULL;

	}
	~Building() {
		if (hasTurret()) delete turret;
	}

	void trackChopper(int cx, int cy) {
		if (hasTurret())
			turret->trackChopper(cx, cy);
	}

	void incrementX(int amount) {
		x -= amount;
		if (hasTurret()) turret->incrementX(amount);
	}

	void draw(XInfo &xInfo) {
		XDrawRectangle(xInfo.display, xInfo.window, xInfo.gc[1], x, y, width, height);
		// cout << "drawing building at " << x << " " << y << " size " << width << "x" << height << endl;
		if (hasTurret()) turret->draw(xInfo);
	}

	void updateScreenSize(int height, int width) {
		int oldH = screenHeight;
		int oldW = screenWidth;
		screenHeight = height;
		screenWidth = width;
		resize(oldH, oldW, height, width);
	}

	int getX() {
		return x;
	}

	int getWidth() {
		return width;
	}

	bool hitDetect(HitBox t) {
		if ((t.x1 >= x && t.x1 <= x + width) || (t.x2 >= x && t.x2 <= x + width)) {
			if ((t.y1 >= y && t.y1 <= y + height) || (t.y2 >= y && t.y2 <= y + height))
				return true;
		}
		if (hasTurret())
			return turret->hitDetect(t);
		return false;
	}

	bool hasTurret() {
		return (turret != NULL);
	}

	Missile* shoot(int scrollSpeed) {
		if (hasTurret())
			return turret->shoot(scrollSpeed);
		return NULL;
	}

	HitBox getHitBox() {
		HitBox h = (HitBox) {x, y, x + width, y + height};
		return h;
	}

	bool checkBomb(HitBox t) {
		if (hasTurret()) {
			if (turret->hitDetect(t)) {
				delete turret;
				turret = NULL;
				return true;
			}
		}
		return hitDetect(t);
	}

protected:
	void resize(int oldH, int oldW, int h, int w) {
		x = x * ((double)w / (double)oldW);
		y = y * ((double)h / (double)oldH);
		height = h * heightPercent;
		width = w / WIDTH_RATIO;

		if (hasTurret()) {
			int tw = width / 2;
			turret->resize(x + (width - tw) / 2, y - tw, tw, tw);
		}
	}

	void initTurret() {
		int turretWidth = width / 2;
		int turretX = x + (width - turretWidth) / 2;
		int turretY = y - turretWidth;
		turret = new Turret(turretX, turretY, turretWidth, turretWidth);
	}
};

class City: public DrawObject {
protected:
	int screenHeight;
	int screenWidth;
	int scrollSpeed;
	deque<Building*> buildings;
	unordered_set<Missile*> missiles;
	unordered_set<Bomb*> bombs;

public:
	City(int screenHeight, int screenWidth, int scrollSpeed) : screenHeight(screenHeight), screenWidth(screenWidth), scrollSpeed(scrollSpeed) {}
	~City() {
		while (!buildings.empty()) {
			Building* t = buildings.front();
			buildings.pop_front();
			delete t;
		}
		for (unordered_set<Missile*>::iterator it = missiles.begin(); it != missiles.end(); it++) {
			Missile* m = *it;
			missiles.erase(it);
			delete m;
		}
		for (unordered_set<Bomb*>::iterator it = bombs.begin(); it != bombs.end(); it++) {
			Bomb* b = *it;
			bombs.erase(it);
			delete b;
		}
	}

	void trackBomb(Bomb* b) {
		if (b != NULL)
			bombs.insert(b);
	}

	int checkBombs() {
		int hits = 0;
		for(unordered_set<Bomb*>::iterator bombIt = bombs.begin(); bombIt != bombs.end(); bombIt++) {
			for(deque<Building*>::iterator buildIt = buildings.begin(); buildIt != buildings.end(); buildIt++) {
				if ((*buildIt)->checkBomb((*bombIt)->getHitBox())) {
					hits += 1;
					Bomb* b = *bombIt;
					bombs.erase(bombIt);
					delete b;
					break;
				}
			}
		}
		return hits;
	}

	void cleanupMissiles() {
		for(unordered_set<Missile*>::iterator mIter = missiles.begin(); mIter != missiles.end(); mIter++) {
			if (!(*mIter)->onscreen(screenHeight, screenWidth)) {
				Missile* m = *mIter;
				missiles.erase(mIter);
				delete m;
				continue;
				// cout << "Missile deleted" << endl;
			}

			// Check for building collisions
			for(deque<Building*>::iterator bIter = buildings.begin(); bIter != buildings.end(); bIter++) {	
				if ((*mIter)->hitDetect((*bIter)->getHitBox())) {
					Missile* m = *mIter;
					missiles.erase(mIter);
					delete m;
					break;
				}
			}
		}
	}

	void shoot() {
		// int r = rand() % buildings.size();
		// Missile* m = buildings.at(r)->shoot(scrollSpeed);
		// if (m != NULL)
		// 	missiles.insert(m);
		for(deque<Building*>::iterator it = buildings.begin(); it != buildings.end(); it++) {
			if (rand() % 3 != 0)
				continue;
			Missile* m = (*it)->shoot(scrollSpeed);
			if (m != NULL)
				missiles.insert(m);
		}
	}

	void trackChopper(int cx, int cy) {
		for (deque<Building*>::iterator it = buildings.begin(); it != buildings.end(); it++)
			(*it)->trackChopper(cx, cy);
	}

	int scroll() {
		if (buildings.empty()) {
			spawnBuilding();
			return 0;
		}

		for(deque<Building*>::iterator it = buildings.begin(); it != buildings.end(); it++)
			(*it)->incrementX(scrollSpeed);

		if (buildings.front()->getX() + buildings.front()->getWidth() < 0) {
			Building* t = buildings.front();
			buildings.pop_front();
			delete t;
		}

		if (screenWidth >= buildings.back()->getX() + buildings.back()->getWidth())
			spawnBuilding();

		for(unordered_set<Missile*>::iterator it = missiles.begin(); it != missiles.end(); it++)
			(*it)->move();
		for(unordered_set<Bomb*>::iterator it = bombs.begin(); it != bombs.end(); it++)
			(*it)->move();

		return scrollSpeed;
	}

	void spawnBuilding() {
		int r = rand() % 6000;
		int r2 = rand() % 3;
		// cout << screenHeight << " " << screenWidth << endl;
		Building* b = new Building(screenHeight, screenWidth, (0.1 + ((double)r / 10000)), (r2 == 0 ? true : false));
		buildings.push_back(b);
	}

	void updateScreenSize(int height, int width) {
		// cout << "city screen resized from " << screenWidth << "x" << screenHeight << " to " << width << "x" << height << endl;
		screenHeight = height;
		screenWidth = width;
		for(deque<Building*>::iterator it = buildings.begin(); it != buildings.end(); it++)
			(*it)->updateScreenSize(height, width);
	}

	void draw(XInfo &xInfo) {
		for(deque<Building*>::iterator it = buildings.begin(); it != buildings.end(); it++)
			(*it)->draw(xInfo);
		for(unordered_set<Missile*>::iterator it = missiles.begin(); it != missiles.end(); it++)
			(*it)->draw(xInfo);
		for(unordered_set<Bomb*>::iterator it = bombs.begin(); it != bombs.end(); it++)
			(*it)->draw(xInfo);
	}

	bool crashDetect(HitBox t) {
		for (deque<Building*>::iterator it = buildings.begin(); it != buildings.end(); it++)
			if ((*it)->hitDetect(t)) return true;
		return false;
	}

	int missileDetectHelicopter(HitBox t) {
		int hits = 0;
		for (unordered_set<Missile*>::iterator it = missiles.begin(); it != missiles.end(); it++) {
			if ((*it)->hitDetect(t)) {
				hits += 1;
				// cout << "missile hit" << endl;
				Missile* m = (*it);
				missiles.erase(it);
				delete m;
			}
		}
		return hits;
	}

	void increaseScrollSpeed() {
		scrollSpeed += 1;
	}

	void setScrollSpeed(int s) {
		scrollSpeed = s;
	}
};

class Helicopter: public DrawObject {
protected:
	static const int MAX_BOMBS = 4;
	static const int BOMB_REGEN = 3;
	static const int HEIGHT_RATIO = 20;
	static const int WIDTH_RATIO = 16;
	static const int X_SPEED_RATIO = 200;
	static const int Y_SPEED_RATIO = 200;
	static const int ACCELERATION_RATIO = 3;
	static constexpr double DRAG_COEFFICIENT = -0.1;

	int x;
	int y;
	int copterHeight;
	int copterWidth;
	int health;

	unsigned int screenHeight;
	unsigned int screenWidth;

	double xVelocity;
	double yVelocity;
	double xDelta;
	double yDelta;
	double maxSpeedX;
	double maxSpeedY;
	double xAcceleration;
	double yAcceleration;

	deque<time_t> bombTimers;

public:
	Helicopter(int x, int y, int screenHeight, int screenWidth) : x(x), y(y), screenHeight(screenHeight), screenWidth(screenWidth), xVelocity(0), yVelocity(0), xDelta(0), yDelta(0), health(10) {
		resize();
	}
	Helicopter(int screenHeight, int screenWidth) : x(0), y(0), screenHeight(screenHeight), screenWidth(screenWidth), xVelocity(0), yVelocity(0), xDelta(0), yDelta(0), health(10) {
		resize();
	}
	~Helicopter(){}

	Bomb* dropBomb() {
		if (bombCount() <= 0)
			return NULL;
		Bomb* b = new Bomb(x + 0.5*copterWidth, y + 0.5*copterHeight, xVelocity, 1);
		bombTimers.push_back(time(NULL));
		return b;
	}

	int bombCount() {
		return MAX_BOMBS - bombTimers.size();
	}

	void restockBombs() {
		if (bombTimers.empty())
			return;
		time_t t = time(NULL);
		// cout << "clock: " << t << " bombTimer: " << bombTimers.front() << endl;
		if (t - bombTimers.front() >= BOMB_REGEN)
			bombTimers.pop_front();
	}

	void updateScreenSize(int height, int width) {
		// cout << "heli screen resized from " << screenWidth << "x" << screenHeight << " to " << width << "x" << height << endl;
		screenHeight = height;
		screenWidth = width;
		resize();
	}

	void accelerateX(bool forward) {
		xVelocity += xAcceleration * (forward ? 1 : -1);

		if (xVelocity > maxSpeedX) {
			xVelocity = maxSpeedX;
			return;
		}
		if (xVelocity < maxSpeedX * -1)
			xVelocity = maxSpeedX * -1;
	}

	void accelerateY(bool forward) {
		yVelocity += yAcceleration * (forward ? -1 : 1);

		if (yVelocity > maxSpeedY) {
			yVelocity = maxSpeedY;
			return;
		}
		if (yVelocity < maxSpeedY * -1)
			yVelocity = maxSpeedY * -1;
	}

	void move() {
		double intPart = 0;
		double fracPart = modf(xVelocity, &intPart);

		int d = roundTowardsZero(xDelta);
		x += (int)intPart + d;
		xDelta += fracPart - d;

		if (x < 0 || (x + copterWidth) > screenWidth) {
			xVelocity = 0;
			xDelta = 0;
			if (x < 0)
				x = 0;
			else
				x = screenWidth - copterWidth;
		}

		intPart = 0;
		fracPart = modf(yVelocity, &intPart);

		d = roundTowardsZero(yDelta);
		y += (int)intPart + d;
		yDelta += fracPart - d;

		if (y < 0 || (y + copterHeight) > screenHeight) {
			yVelocity = 0;
			yDelta = 0;
			if (y < 0)
				y = 0;
			else
				y = screenHeight - copterHeight;
		}

		applyDrag();
	}

	void draw(XInfo &xInfo) {
		int bodyHeight = copterHeight-10;
		int bodyWidth = copterWidth-10;
		int bladeHeight = copterHeight - bodyHeight - 3;
		int bladeWidth = bodyWidth - 4;
		int tailSize = copterWidth - bodyWidth - 2;

		// Bounding box
		// XDrawRectangle(xInfo.display, xInfo.window, xInfo.gc[0], x, y, copterWidth, copterHeight);
		
		XDrawArc(
			xInfo.display, xInfo.window, xInfo.gc[0], 
			x + (copterWidth - bodyWidth), y + (copterHeight - bodyHeight), 
			bodyWidth, bodyHeight, 
			0, 360*64
		);
		XDrawArc(
			xInfo.display, xInfo.window, xInfo.gc[0],
			x + (copterWidth - ((bladeWidth + bodyWidth) / 2)), y,
			bladeWidth, bladeHeight,
			0, 360*64
		);
		XDrawLine(
			xInfo.display, xInfo.window, xInfo.gc[0],
			x + copterWidth - bodyWidth + (bodyWidth / 2), y + copterHeight - bodyHeight,
			x + copterWidth - bodyWidth + (bodyWidth / 2), y + (bladeHeight / 2)
		);
		XDrawArc(
			xInfo.display, xInfo.window, xInfo.gc[0],
			x, y + ((copterHeight - tailSize) / 2),
			tailSize, tailSize,
			0, 360*64
		);
		XDrawLine(
			xInfo.display, xInfo.window, xInfo.gc[0],
			x + (copterWidth - bodyWidth), y + copterHeight - (bodyHeight / 2),
			x + (tailSize / 2), y + copterHeight / 2
		);

		// Health bar
		XDrawRectangle(xInfo.display, xInfo.window, xInfo.gc[2], 10, 10, 100, 20);
		XFillRectangle(xInfo.display, xInfo.window, xInfo.gc[0], 10, 10, 10 * health, 20);
		
		// Bomb Counter
		for(int i = 0; i < bombCount(); i++)
			XFillArc(xInfo.display, xInfo.window, xInfo.gc[0], 10 + 10*i, 40, 8, 8, 0, 360*64);
	}

	string debugInfo() {
		stringstream info;
		info << "x: " << x << " y: " << y << " vx: " << xVelocity << " vy: " << yVelocity; 
		return info.str();
	}

	HitBox getHitBox() {
		HitBox h = (HitBox) {x, y, x+copterWidth, y+copterHeight};
		return h;
	}

	int getCentreX() {
		return x + 0.5*copterWidth;
	}

	int getCentreY() {
		return y + 0.5*copterHeight;
	}

	bool missileHit(int hits) {
		health -= hits;
		if (health <= 0)
			return true;
		return false;
	}

protected:
	void applyDrag() {
		double threshold = maxSpeedX / 10;
		if (xVelocity < threshold && xVelocity > threshold * -1)
			xVelocity = 0;
		else
			xVelocity += xVelocity * DRAG_COEFFICIENT;

		threshold = maxSpeedY / 10;
		if (yVelocity < threshold && yVelocity > threshold * -1)
			yVelocity = 0;
		else
			yVelocity += yVelocity * DRAG_COEFFICIENT;
	}

	// Adjust variables so screen size doesn't affect gameplay
	void resize() {
		copterHeight = screenHeight / HEIGHT_RATIO;
		copterWidth = screenWidth / WIDTH_RATIO;
		maxSpeedY = screenHeight / Y_SPEED_RATIO;
		maxSpeedX = screenWidth / X_SPEED_RATIO;
		xAcceleration = maxSpeedX / ACCELERATION_RATIO;
		yAcceleration = maxSpeedY / ACCELERATION_RATIO;
	}
};

// Error message function
void error(string str) {
	cerr << str << endl;
	exit(0);
}

static double timeDiff(clock_t c1, clock_t c2) {
	double diffTicks = c1 - c2;
	return (diffTicks / (CLOCKS_PER_SEC / 1000000));
}

static int roundTowardsZero(double x) {
	if (x < 0) return ceil(x);
	return floor(x);
}

class Game {
private:
	XInfo xInfo;
	Helicopter* heli;
	City* city;
	int windowHeight;
	int windowWidth;
	clock_t lastRepaint;
	clock_t end;
	unordered_map<int, bool> smoothKeys;
	unsigned long score;

public:
	Game(XInfo &xInfo) : xInfo(xInfo), score(0) {
		XWindowAttributes windowInfo;
		XGetWindowAttributes(xInfo.display, xInfo.window, &windowInfo);
		windowHeight = windowInfo.height;
		windowWidth = windowInfo.width;

		smoothKeys.insert(make_pair<int, bool>(XK_Right, false));
		smoothKeys.insert(make_pair<int, bool>(XK_Left, false));
		smoothKeys.insert(make_pair<int, bool>(XK_Up, false));
		smoothKeys.insert(make_pair<int, bool>(XK_Down, false));

		heli = new Helicopter(50, 50, windowHeight, windowWidth);
		city = new City(windowHeight, windowWidth, 2);
	}
	~Game() {
		delete heli;
		delete city;
	}

	void reset() {
		delete heli;
		delete city;

		score = 0;
		smoothKeys[XK_Right] = false;
		smoothKeys[XK_Left] = false;
		smoothKeys[XK_Up] = false;
		smoothKeys[XK_Down] = false;

		heli = new Helicopter(50, 50, windowHeight, windowWidth);
		city = new City(windowHeight, windowWidth, 1);
	}

	void checkScreenSize() {
		XWindowAttributes windowInfo;
		XGetWindowAttributes(xInfo.display, xInfo.window, &windowInfo);
		windowHeight = windowInfo.height;
		windowWidth = windowInfo.width;
		resizeWindow(windowHeight, windowWidth);
	}

	int run() {
		checkScreenSize();
		bool stop = false;
		int currentSpeed = 0;
		while (!stop) {
			if (XPending(xInfo.display) > 0) {
				XEvent event;
				XNextEvent(xInfo.display, &event);

				KeySym key;
				switch(event.type) {
					case FocusOut:
						XAutoRepeatOn(xInfo.display);
						stop = pause();
						break;

					case FocusIn:
						XAutoRepeatOff(xInfo.display);
						break;

					case Expose:
						repaint();
						break;

					case ConfigureNotify:
						if (event.xconfigure.height != windowHeight || event.xconfigure.width != windowWidth) {
							resizeWindow(event.xconfigure.height, event.xconfigure.width);
						}
						break;

					case KeyRelease:
						key = XLookupKeysym(&event.xkey, 0);
						if (key == XK_Right || key == XK_Left || key == XK_Up || key == XK_Down) {
							smoothKeys[key] = false;
						}
						break;

					case KeyPress:
						key = XLookupKeysym(&event.xkey, 0);					
						if (key == XK_Right || key == XK_Left || key == XK_Up || key == XK_Down) {
							smoothKeys[key] = true;
						}
						if (key == XK_q)
							stop = true;
						if (key == XK_space)
							city->trackBomb(heli->dropBomb());
						if (key == XK_f || key == XK_F)
							stop = pause();
						break;
				}
			}

			// Give forward movement priority if both keys are held
			if (smoothKeys[XK_Right])
				heli->accelerateX(true);
			else if (smoothKeys[XK_Left])
				heli->accelerateX(false);

			// Give upwards movement priority if both keys are held
			if (smoothKeys[XK_Up])
				heli->accelerateY(true);
			else if (smoothKeys[XK_Down])
				heli->accelerateY(false);

			heli->move();
			heli->restockBombs();

			currentSpeed = city->scroll();
			score += currentSpeed;
			score += 100 * city->checkBombs();
			if (score / 10000 > currentSpeed)
				city->increaseScrollSpeed();

			// cout << "\rscore: " << score << flush;

			if (score % (2*TARGET_FPS) == 0) {
				// cout << "shoot" << endl;
				city->shoot();
			}
				

			city->trackChopper(heli->getCentreX(), heli->getCentreY());
			city->cleanupMissiles();

			HitBox t = heli->getHitBox();
			if (city->crashDetect(t)) {
				// cout << "collision" << endl;
				stop = true;
			}
			if (heli->missileHit(city->missileDetectHelicopter(t))) {
				// cout << "DEAD";
				stop = true;
			}


			end = clock();
			repaint();

			if (XPending(xInfo.display) == 0)
				usleep((1000000 / TARGET_FPS) - timeDiff(end, lastRepaint));
		}

		return score;
	}

	void drawSplashScreen() {
		XClearWindow(xInfo.display, xInfo.window);

		deque<string> s = {"CS349 Assignment 1", "Arrow Keys: move helicopter", "Space: drop bomb", "f: pause", "q: quit", "Dane Carr", "873", "Press any key to begin"};

		for(int i = 0; i < s.size(); i++) {
			XDrawString (xInfo.display, xInfo.window, xInfo.gc[0], windowWidth / 2, windowHeight / 2 + 15*i, s[i].c_str(), s[i].length());
		}

		XFlush(xInfo.display);
	}

	void drawGameOverScreen(int score) {
		XClearWindow(xInfo.display, xInfo.window);

		deque<string> s = {"GAME OVER", static_cast<ostringstream*>(&(ostringstream() << "score: " << score))->str(), "Play again? (y/n)"};
		for (int i = 0; i < s.size(); i++) {
			XDrawString(xInfo.display, xInfo.window, xInfo.gc[0], windowWidth / 2, windowHeight / 2 + 15*i, s[i].c_str(), s[i].length());
		}

		XFlush(xInfo.display);
	}

private:
	void repaint() {
		XClearWindow(xInfo.display, xInfo.window);

		heli->draw(xInfo);
		city->draw(xInfo);
		drawScore(xInfo);

		XFlush(xInfo.display);

		lastRepaint = clock();
	}

	void drawScore(XInfo &xInfo) {
		string s = static_cast<ostringstream*>(&(ostringstream() << score))->str();
		XDrawString(xInfo.display, xInfo.window, xInfo.gc[0], 10, 65, s.c_str(), s.length());
	}

	bool pause() {
		while (true) {
			if (XPending(xInfo.display) > 0) {
				XEvent event;
				XNextEvent(xInfo.display, &event);

				KeySym key;
				switch(event.type) {
					case Expose:
						repaintPauseScreen();
						break;

					case ConfigureNotify:
						if (event.xconfigure.height != windowHeight || event.xconfigure.width != windowWidth) {
							resizeWindow(event.xconfigure.height, event.xconfigure.width);
						}
						break;

					case KeyPress:
						key = XLookupKeysym(&event.xkey, 0);
						if (key == XK_f || key == XK_F)
							return false;
						if (key == XK_q || key == XK_Q)
							return true;
				}
			}

			end = clock();
			repaintPauseScreen();

			if (XPending(xInfo.display) == 0)
				usleep((1000000 / TARGET_FPS) - timeDiff(end, lastRepaint));
		}
	}

	void repaintPauseScreen() {
		XClearWindow(xInfo.display, xInfo.window);

		string str = "Paused";
		XDrawString(xInfo.display, xInfo.window, xInfo.gc[0], 10, 10, str.c_str(), str.length());

		XFlush(xInfo.display);

		lastRepaint = clock();
	}

	void resizeWindow(int newHeight, int newWidth) {
		windowHeight = newHeight;
		windowWidth = newWidth;

		heli->updateScreenSize(newHeight, newWidth);
		city->updateScreenSize(newHeight, newWidth);
	}
};

void cleanup(XInfo &xInfo) {
	XAutoRepeatOn(xInfo.display);
	XCloseDisplay(xInfo.display);
}

void initX(int argc, char* argv[], XInfo &xInfo) {
	XSizeHints hints;
	unsigned long white;
	unsigned long black;

	// Open display
	xInfo.display = XOpenDisplay("");
	if (!xInfo.display)
		error("Can't open display.");

	xInfo.screen = DefaultScreen(xInfo.display);

	white = XWhitePixel(xInfo.display, xInfo.screen);
	black = XBlackPixel(xInfo.display, xInfo.screen);

	hints.x = 100;
	hints.y = 100;
	hints.width = 800;
	hints.height = 600;
	hints.flags = PPosition | PSize;

	xInfo.window = XCreateSimpleWindow(
		xInfo.display,
		DefaultRootWindow(xInfo.display),
		hints.x,
		hints.y,
		hints.width,
		hints.height,
		BORDER,
		black,
		white
	);

	XSetStandardProperties(
		xInfo.display,
		xInfo.window,
		"base.cc",
		"X11 beginning",
		None,
		argv,
		argc,
		&hints
	);

	// Create a graphics context
	int i = 0;
	xInfo.gc[i] = XCreateGC(xInfo.display, xInfo.window, 0, 0);
	XSetForeground(xInfo.display, xInfo.gc[i], BlackPixel(xInfo.display, xInfo.screen));
	XSetBackground(xInfo.display, xInfo.gc[i], WhitePixel(xInfo.display, xInfo.screen));
	XSetFillStyle(xInfo.display, xInfo.gc[i], FillSolid);
	XSetLineAttributes(xInfo.display, xInfo.gc[i], 1, LineSolid, CapButt, JoinRound);

	i = 1;
	xInfo.gc[i] = XCreateGC(xInfo.display, xInfo.window, 0, 0);
	XSetForeground(xInfo.display, xInfo.gc[i], BlackPixel(xInfo.display, xInfo.screen));
	XSetBackground(xInfo.display, xInfo.gc[i], WhitePixel(xInfo.display, xInfo.screen));
	XSetFillStyle(xInfo.display, xInfo.gc[i], FillSolid);
	XSetLineAttributes(xInfo.display, xInfo.gc[i], 2, LineSolid, CapButt, JoinRound);

	i = 2;
	xInfo.gc[i] = XCreateGC(xInfo.display, xInfo.window, 0, 0);
	XSetForeground(xInfo.display, xInfo.gc[i], BlackPixel(xInfo.display, xInfo.screen));
	XSetBackground(xInfo.display, xInfo.gc[i], WhitePixel(xInfo.display, xInfo.screen));
	XSetFillStyle(xInfo.display, xInfo.gc[i], FillSolid);
	XSetLineAttributes(xInfo.display, xInfo.gc[i], 4, LineSolid, CapButt, JoinRound);

	XSelectInput(xInfo.display, xInfo.window, KeyPressMask | KeyReleaseMask | ExposureMask | FocusChangeMask | StructureNotifyMask);

	XMapRaised(xInfo.display, xInfo.window);

	XAutoRepeatOff(xInfo.display);

	XFlush(xInfo.display);
	sleep(2);
}

int main(int argc, char* argv[]) {
	XInfo xInfo;
	initX(argc, argv, xInfo);


	Game g(xInfo);
	g.drawSplashScreen();

	XEvent event;
	do {
		XNextEvent(xInfo.display, &event);
		if (event.type == Expose)
			g.drawSplashScreen();
	} while(event.type != KeyPress);

	bool stop = false;
	while (!stop) {
		int score = g.run();
		g.drawGameOverScreen(score);
		// cout << "final score: " << score << endl;

		while(true) {
			XNextEvent(xInfo.display, &event);
			if (event.type == Expose)
				g.drawGameOverScreen(score);
			if (event.type == KeyPress) {
				KeySym key;
				key = XLookupKeysym(&event.xkey, 0);
				if (key == XK_n || key == XK_N) {
					stop = true;
					break;
				}
				if (key == XK_y || key == XK_Y)
					break;
			}
		}
		g.reset();
	}

	cleanup(xInfo);
}