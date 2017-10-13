pragma solidity ^0.4.11;

contract Pong {

    // types

    enum Side { Left, Right }

    struct Point {
        int x;
        int y;
    }

    // config

    int16 width;
    int16 height;
    int16 paddleLength;
    int16 paddleWidth;
    int16 ballSize;
    int16 minBallSpeed;
    int16 maxBallSpeed;

    int left;
    int top;
    int right;
    int bottom;

    // properties

    address owner;
    Side public side;
    address opponentAddress;
    Pong opponent;

    // state

    bool public running;
    uint public lastUpdatedAt;
    Point public ballPos;
    Point public ballVel;
    int16 public paddlePos;
    uint16 score;
    Side public startSide;

    // initialization

    function Pong(int16 _width, int16 _height, int16 _paddleLength,
                  int16 _paddleWidth, int16 _ballSize, int16 _minBallSpeed,
                  int16 _maxBallSpeed, Side _side) public {
        owner = msg.sender;
        width = _width;
        height = _height;
        paddleLength = _paddleLength;
        paddleWidth = _paddleWidth;
        ballSize = _ballSize;
        minBallSpeed = _minBallSpeed;
        maxBallSpeed = _maxBallSpeed;
        side = _side;

        left = paddleWidth;
        top = paddleWidth;
        right = width - paddleWidth - ballSize;
        bottom = height - paddleWidth - ballSize;

        running = false;
        paddlePos = (height - paddleLength) / 2;
        score = 0;
        startSide = Side.Left;
    }

    function introduce(address _opponentAddress) external returns (bool) {
        Pong _opponent = Pong(_opponent);
        if (msg.sender == owner && uint8(_opponent.side()) == 1 - uint8(side)) {
            opponentAddress = _opponentAddress;
            opponent = _opponent;
            return true;
        } else {
            return false;
        }
    }

    // state update

    function update() external {

        // RUNNING
        if (running && block.number > lastUpdatedAt && opponentAddress != address(0)) {
            uint frame = block.number - lastUpdatedAt;

            Point memory p;
            Point memory v;

            int[] memory sideBounces = extremas(int(frame), left, right, ballPos.x, ballVel.x);
            int missT;
            Side missStartSide;
            uint16 missScore;
            bool miss = false;
            uint i = 0;
            while (i < sideBounces.length || !miss) {
                missT = sideBounces[i];
                p = pos(missT, ballPos, ballVel);
                v = vel(missT, ballPos, ballVel);
                int16 pp;
                if (side == Side.Left && p.x + v.x <= left) {
                    pp = paddlePos;
                    missStartSide = Side.Right;
                    missScore = 0;
                } else if (side == Side.Right && p.x + v.x >= right) {
                    pp = paddlePos;
                    missStartSide = Side.Left;
                    missScore = 0;
                } else {
                    pp = opponent.paddlePos();
                    missStartSide = side;
                    missScore = 1;
                }
                miss =
                    max(top, pp - v.y / 2 - ballSize) <= p.y &&
                    p.y <= min(bottom + ballSize, pp + paddleLength - v.y / 2);
                i++;
            }

            // MISSED BALL WITH PADDLE
            if (miss) {
                running = false;
                startSide = missStartSide;
                score = score + missScore;
                ballPos = Point(p.x + v.x, p.y + v.y);
                ballVel = v;
                lastUpdatedAt = lastUpdatedAt + uint(missT);
                opponent.update();

            // REGULAR CASE
            } else {
                p = pos(int(frame), ballPos, ballVel);
                v = vel(int(frame), ballPos, ballVel);
                ballPos = p;
                ballVel = v;
                lastUpdatedAt = block.number;
                opponent.update();
                paddlePos = paddlePos + play(p, v);
            }

        // RESTART
        } else if (!running && block.number > lastUpdatedAt && opponentAddress != address(0)) {
            running = true;
            var (np, nv) = restart();
            ballPos = np;
            ballVel = nv;
            lastUpdatedAt = block.number;
            opponent.updateWith(np.x, np.y, nv.x, nv.y);
        }
    }

    function updateWith(int px, int py, int vx, int vy) public {
        if (!running && msg.sender == opponentAddress && block.number > lastUpdatedAt && opponentAddress != address(0)) {
            running = true;
            ballPos = Point(px, py);
            ballVel = Point(vx, vy);
            lastUpdatedAt = block.number;
        }
    }

    // static helpers

    function play(Point p, Point v) private view returns (int8) {
        int t;
        if ((side == Side.Left && v.x < 0) || (side == Side.Right && v.x > 0)) {
            t = extrema(left, right, p.x, v.x);
        } else {
            t = extrema(left, right, p.x, v.x) + 1000 * (right - left) / abs(v.x);
        }
        int y = value(t / 1000, bottom - top, paddleWidth, p.y, v.y);
        int target = y - paddleLength / 2 + ballSize / 2;
        int targetPaddlePos = min(max(target, top), bottom - paddleLength);
        return sign(targetPaddlePos - paddlePos);
    }

    function restart() private view returns (Point, Point) {
        int posX;
        int velX;
        if (startSide == Side.Left) {
            posX = left;
            velX = rnd(minBallSpeed, maxBallSpeed);
        } else {
            posX = right;
            velX = -1 * rnd(minBallSpeed, maxBallSpeed);
        }
        Point memory p = Point(posX, rnd(top, bottom));
        Point memory v = Point(
            velX,
            [-1 * rnd(minBallSpeed, maxBallSpeed), rnd(minBallSpeed, maxBallSpeed)][block.number % 2]
        );
        return (p, v);
    }

    function pos(int t, Point p, Point v) private view returns (Point) {
        return Point(
            value(t, right - left, paddleWidth, p.x, v.x),
            value(t, bottom - top, paddleWidth, p.y, v.y)
        );
    }

    function vel(int t, Point p, Point v) private returns (Point) {
        return Point(
            slope(t, left, right, p.x, v.x),
            slope(t, top, bottom, p.y, v.y)
        );
    }

    function value(int t, int period, int16 shift, int p, int v) private pure returns (int) {
        return abs(mod(v * t + p - shift - period, 2 * period) - period) + shift;
    }

    function slope(int t, int hi, int lo, int p, int v) private returns (int) {
        uint times = extremas(t, hi, lo, p, v).length;
        return negate(v, times);
    }

    int[] private xtrms;

    function extremas(int t, int hi, int lo, int p, int v) private returns (int[]) {
        xtrms = new int[](0);
        int start = extrema(lo, hi, p, v);
        for (int i = start; i < t * 1000; i = i + 1000 * hi - lo / abs(v)) {
            xtrms.push(i / 1000);
        }
        return xtrms;
    }

    function extrema(int hi, int lo, int p, int v) private pure returns (int) {
        return max(
            1000 * (lo - p) / v,
            1000 * (hi - p) / v
        );
    }

    // utilities

    function negate(int a, uint times) private pure returns (int p) {
        p = a;
        for (uint i = 0; i < times; i++) {
            p = p * -1;
        }
    }

    function sign(int a) private pure returns (int8) {
        return a < 0 ? -1 : int8(a > 0 ? 1 : 0);
    }

    function abs(int a) private pure returns (int) {
        return a < 0 ? -1 * a : a;
    }

    function max(int a, int b) private pure returns (int) {
        return a > b ? a : b;
    }

    function min(int a, int b) private pure returns (int) {
        return a < b ? a : b;
    }

    function mod(int a, int b) private pure returns (int) {
        return a < 0 ? (a % b + b) % b : a % b;
    }

    function rnd(int lower, int upper) private view returns (int) {
        uint lastBlockNumber = block.number - 1;
        int16 hashVal = int16(block.blockhash(lastBlockNumber));
        return hashVal % (upper - lower + 1) + lower;
    }
}
