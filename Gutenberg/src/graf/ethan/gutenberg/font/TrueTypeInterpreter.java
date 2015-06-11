package graf.ethan.gutenberg.font;

/*
 * This class interprets instructions in TrueType fonts.
 */
public class TrueTypeInterpreter {
	
	private TrueTypeFont font;
	private TableGlyph table;
	private TableCVT cvtTable;
	
	private GraphicsState state;
	
	private int pointSize;
	private int resolution;
	private int pixelsPerEm;
	
	private boolean rotated = false;
	private boolean stretched = false;
	
	private int scanType;
	
	public TrueTypeInterpreter(TrueTypeFont font, TableGlyph table, Point[] outline, int pointSize, int resolution) {
		this.font = font;
		
		this.table = table;
		this.cvtTable = font.cvt;
		this.state = new GraphicsState();
		
		state.storage = new int[font.maxp.maxStorage];
		
		this.pointSize = pointSize;
		this.resolution = resolution;
		this.pixelsPerEm = Math.round((((float) pointSize)/72f) * resolution);;
		
		state.zone = new PointF26Dot6[2][];
		state.zone[1] = new PointF26Dot6[outline.length];
		state.original = new PointF26Dot6[2][];
		state.original[1] = new PointF26Dot6[outline.length];
		for(int i = 0; i < outline.length; i++) {
			state.original[1][i] = state.zone[1][i] = new PointF26Dot6(floatToFixed(outline[i].x), floatToFixed(outline[i].y));
			state.original[1][i].onCurve = state.zone[1][i].onCurve = outline[i].onCurve;
		}
		//Execute every instruction.
		while(state.instructionPointer < table.instructionLen) {
			execute();
		}
		
		
	}
	
	public int floatToFixed(float f) {
		return (int) f*64;
	}

	/*
	 * Push bytes from the instruction stream onto the stack.
	 */
	public void pushNBytes() {
		//The number of bytes to push.
		int num = (int) table.getInstruction(state.instructionPointer);
		state.instructionPointer++;
		System.out.println("NUM: " + num);
		for(int i = 0; i < num; i ++) {
			int value = (int) table.getInstruction(state.instructionPointer);
			state.instructionPointer++;
			System.out.println("BYTE: " + value);
			state.push(value, 4);
		}
	}
	
	/*
	 * Push words from the instruction stream onto the stack.
	 */
	public void pushNWords() {
		//The number of words to push.
		int num = (int) table.getInstruction(state.instructionPointer);
		state.instructionPointer++;
		for(int i = 0; i < num; i ++) {
			//Combine two bytes to form a word.
			int num1 = (int) table.getInstruction(state.instructionPointer) << 8;
			state.instructionPointer++;
			int num2 = (int) table.getInstruction(state.instructionPointer);
			state.instructionPointer++;
			state.push(num1+num2, 5);
		}
	}
	
	/*
	 * Push bytes to the stack.
	 */
	public void pushBytes(int num) {
		for(int i = 0; i < num; i ++) {
			int value = (int) table.getInstruction(state.instructionPointer);
			state.instructionPointer++;
			state.push(value, 4);
		}
	}
	
	/*
	 * Push words to the stack.
	 */
	public void pushWords(int num) {
		System.out.println(num);
		for(int i = 0; i < num; i ++) {
			//Combine two bytes to form a word.
			int word1 = (int) table.getInstruction(state.instructionPointer) << 8;
			state.instructionPointer++;
			int word2 = (int) table.getInstruction(state.instructionPointer);
			state.instructionPointer++;
			System.out.println("WORD: " + (word1 + word2));
			state.push(word1+word2, 5);
		}
	}
	
	/*
	 */
	public void copyIndex() {
		int index = ((Number) state.pop()).intValue();
		StackElement element = state.stack.get(index);
		state.stack.push(element);
	}
	
	/*
	 * Remove an element from the specified index and push it to the top of the stack.
	 */
	public void moveIndex() {
		int index = ((Number) state.pop()).intValue();
		StackElement element = state.stack.get(index);
		state.stack.removeElementAt(index);
		state.stack.push(element);
	}
	
	/*
	 * Move element 3 to the top of the stack.
	 */
	public void roll() {
		StackElement element = state.stack.get(2);
		state.stack.removeElementAt(2);
		state.stack.push(element);
	}
	
	/*
	 * Swap the first two elements of the stack.
	 */
	public void swap() {
		StackElement element = state.stack.get(1);
		state.stack.removeElementAt(1);
		state.stack.push(element);
	}
	
	/*
	 * Add the top two stack elements.
	 */
	public void add() {
		int n1 = state.popF26Dot6();
		int n2 = state.popF26Dot6();
		state.push(n1+n2, 6);
	}
	
	/*
	 * Subtract the top stack element from the element below it.
	 */
	public void subtract() {
		int n1 = state.popF26Dot6();
		int n2 = state.popF26Dot6();
		state.push(n2-n1, 6);
	}
	
	/*
	 * Multiply the top two stack elements.
	 */
	public void multiply() {
		long n1 = state.popF26Dot6();
		long n2 = state.popF26Dot6();
		int product = (int) ((n1 * n2) >> 6);
		state.push(product, 6);
	}
	
	/*
	 * Divide the top two stack elements.
	 */
	public void divide() {
		int n1 = state.popF26Dot6();
		int n2 = state.popF26Dot6();
		int quotient = ((n2 << 6)/n1);
		state.push(quotient, 6);
	}
	
	/*
	 * Push the absolute value of the top element.
	 */
	public void absoluteValue() {
		int n = state.popF26Dot6();
		state.push((int) Math.abs(n), 6);
	}
	
	/*
	 * Push the negation of the top element.
	 */
	public void negate() {
		int n = state.popF26Dot6();
		state.push(-(int) Math.abs(n), 6);
	}
	
	/*
	 * Push the floor value of the top element.
	 */
	public void floor() {
		int n = state.popF26Dot6();
		state.push(n / 64, 6);
	}
	
	/*
	 * Push the ceiling value of the top element.
	 */
	public void ceiling() {
		int n = state.popF26Dot6();
		state.push((n / 64) + 1, 6);
	}
	
	/*
	 * Push the larger of the top two stack elements.
	 */
	public void maximum() {
		int e1 = state.popF26Dot6();
		int e2 = state.popF26Dot6();
		if(e1 > e2) {
			state.push(e1, 6);
		}
		else {
			state.push(e2, 6);
		}
	}
	
	/*
	 * Push the lesser of the top two stack elements.
	 */
	public void minimum() {
		int e1 = state.popF26Dot6();
		int e2 = state.popF26Dot6();
		if(e1 < e2) {
			state.push(e1, 6);
		}
		else {
			state.push(e2, 6);
		}
	}
	
	/*
	 *Less Than logical test.
	 */
	public void lt() {
		int e1 = state.popF26Dot6();
		int e2 = state.popF26Dot6();
		if(e1 < e2) {
			state.push(1,  4);
		}
		else {
			state.push(0, 4);
		}
	}
	
	/*
	 * Less Than or Equal To logical test.
	 */
	public void lteq() {
		int e1 = state.popF26Dot6();
		int e2 = state.popF26Dot6();
		if(e1 <= e2) {
			state.push(1,  4);
		}
		else {
			state.push(0, 4);
		}
	}
	
	/*
	 * Greater Than logical test.
	 */
	public void gt() {
		int e1 = state.popF26Dot6();
		int e2 = state.popF26Dot6();
		if(e1 > e2) {
			state.push(1,  4);
		}
		else {
			state.push(0, 4);
		}
	}
	
	/*
	 * Greater Than or Equal To logical test.
	 */
	public void gteq() {
		int e1 = state.popF26Dot6();
		int e2 = state.popF26Dot6();
		if(e1 >= e2) {
			state.push(1,  4);
		}
		else {
			state.push(0, 4);
		}
	}
	
	/*
	 * Equal To logical test.
	 */
	public void eq() {
		int e1 = state.popF26Dot6();
		int e2 = state.popF26Dot6();
		if(e1 == e2) {
			state.push(1,  4);
		}
		else {
			state.push(0, 4);
		}
	}
	
	/*
	 * Not Equal To logical test.
	 */
	public void neq() {
		int e1 = state.popF26Dot6();
		int e2 = state.popF26Dot6();
		if(e1 != e2) {
			state.push(1,  4);
		}
		else {
			state.push(0, 4);
		}
	}
	
	/*
	 * Odd logical test.
	 */
	public void odd() {
		int e1 = state.popF26Dot6();
		if((round(e1) >> 6)%2 == 1) {
			state.push(1, 4);
		}
		else {
			state.push(0,  4);
		}
	}
	
	/*
	 * Even logical test
	 */
	public void even() {
		int e1 = state.popF26Dot6();
		if((round(e1) >> 6)%2 == 0) {
			state.push(1, 4);
		}
		else {
			state.push(0,  4);
		}
	}
	
	/*
	 * And logical operation.
	 */
	public void and() {
		int e1 = state.popF26Dot6();
		int e2 = state.popF26Dot6();
		if(e1 != 0f && e2 != 0f) {
			state.push(1, 4);
		}
		else {
			state.push(0,  4);
		}
	}
	
	/*
	 * Or logical operation.
	 */
	public void or() {
		int e1 = state.popF26Dot6();
		int e2 = state.popF26Dot6();
		if(e1 != 0f || e2 != 0f) {
			state.push(1, 4);
		}
		else {
			state.push(0,  4);
		}
	}
	
	/*
	 * Not logical operation.
	 */
	public void not() {
		int e = state.popF26Dot6();
		if(e != 0f) {
			state.push(0, 4);
		}
		else {
			state.push(1, 4);
		}
	}
	
	/*
	 * Conditional Test.
	 */
	public void ifTest() {
		int nextElse = 0;
		int end = 0;
		int depth = 1;
		int count = 0;
		//Find the corresponding else and eif commands. Makes sure not to return nested conditionals, only the ones associated with the current command.
		while(depth != 0) {
			int instruction = table.getInstruction(state.instructionPointer + count);
			if(instruction == 0x58) {
				depth++;
			}
			else if(instruction == 0x1B) {
				if(depth == 1) {
					nextElse = count;
				}
			}
			else if(instruction == 0x59) {
				depth--;
				if(depth == 0) {
					end = count;
				}
			}
			count++;
		}
		
		//Go to the else statement.
		if(((Number) state.pop()).intValue() == 0) {
			jump(nextElse-1);
		}
		else {
			//If no else, execute everything.
			if(nextElse == 0) {
				for(int i = 1; i < end; i++) {
					execute();
				}
			}
			//If there is an else, execute the if portion and skip the else portion.
			else {
				for(int i = 1; i < nextElse; i++) {
					execute();
				}
				jump(end-nextElse+1);
			}
		}
	}
	
	/*
	 * Else statement.
	 */
	public void elseTest() {
		int end = 0;
		int depth = 1;
		int count = 0;
		//Find when the block ends.
		while(depth != 0) {
			int instruction = table.getInstruction(state.instructionPointer + count);
			if(instruction == 0x58) {
				depth++;
			}
			else if(instruction == 0x59) {
				depth--;
				if(depth == 0) {
					end = count;
				}
			}
			count++;
		}
		//Execute the code within the block.
		for(int i = 0; i < end; i++) {
			execute();
		}
	}
	
	//Move the instruction pointer.
	public void jump(int offset) {
		state.instructionPointer += offset;
	}
	
	//Jump if the popped value is true.
	public void jrof() {
		if(((Number) state.pop()).intValue() == 0) {
			jump((int) state.pop());
		}
	}
	
	//Jump if the popped value is false.
	public void jrot() {
		if(((Number) state.pop()).intValue() != 0) {
			jump(((Number) state.pop()).intValue());
		}
	}
	
	//Jump the specified ammount.
	public void jmpr() {
		jump(((Number) state.pop()).intValue());
	}
	
	//Execute a function multiple times.
	public void loopCall() {
		
	}
	
	/*
	 * Functions for setting zone pointers.
	 */
	public void setZonePointer(int num) {
		int val = ((Number) state.pop()).intValue();
		switch(num) {
		case 0:
			state.zp0 = val;
			break;
		case 1:
			state.zp1 = val;
			break;
		case 2:
			state.zp2 = val;
			break;
		case 3:
			//Set all of the zone pointers.
			state.zp0 = val;
			state.zp1 = val;
			state.zp2 = val;
			break;
		}
	}
	
	/*
	 * Set the reference points.
	 */
	public void setReferencePoint(int num) {
		int val = ((Number) state.pop()).intValue();
		System.out.println(val);
		switch(num) {
		case 0:
			state.rp[0] = val;
			break;
		case 1:
			state.rp[1] = val;
			break;
		case 2:
			state.rp[2] = val;
			break;
		}
	}
	
	/*
	 * Switch a series of points from on curve to off curve or vice versa.
	 */
	public void flipPoint() {
		for(int i = 0; i < state.loop; i++) {
			int point = (int) state.pop();
			//Flip the point.
			state.zone[state.zp0][point].onCurve = !state.zone[state.zp0][point].onCurve;
		}
	}
	
	/*
	 * Flip a range of points onCurve to the specified value.
	 */
	public void flipRange(boolean val) {
		int max = (int) state.pop();
		int min = (int) state.pop();
		for(int i = min; i <= max; i++) {
			state.zone[state.zp0][i].onCurve = val;
		}
	}
	
	/*
	 * Set the Round State
	 */
	public void setRoundState(int type) {
		switch(type) {
		case 0:
			//Rounding Off:
			state.roundingOn = false;
			break;
		case 1:
			//Round To Grid
			state.roundingOn = true;
			state.period = 0x00000040;
			state.phase = 0;
			state.threshold = 0x00000020;
			break;
		case 2:
			//Round To Half Grid
			state.roundingOn = true;
			state.period = 0x00000040;
			state.phase = 0x00000020;
			state.threshold = 0x00000020;
			break;
		case 3:
			//Round To Double Grid
			state.roundingOn = true;
			state.period = 0x00000020;
			state.phase = 0;
			state.threshold = 0x00000010;
			break;
		case 4:
			//Round Up To Grid
			state.roundingOn = true;
			state.period = 0x00000040;
			state.phase = 0;
			state.threshold = -1;
			break;
		case 5:
			//Round Down To Grid
			state.roundingOn = true;
			state.period = 0x00000040;
			state.phase = 0;
			state.threshold = 0;
			break;
		}
	}
	
	/*
	 * Set the round state (for 45 degree angles).
	 */
	public void set45Round() {
		int n = ((Number) state.pop()).intValue();
		int period = (n >> 6) & 3;
		int phase = (n >> 4) & 3;
		int threshold = n & 15;
		
		//The period is determined in the first 2 pixels of the final byte.
		switch(period) {
		case 0:
			//Sqrt(2)/2 Pixels.
			state.period = 0x0000002D;
			break;
		case 1: 
			//Sqrt(2) Pixels.
			state.period = 0x0000005A;
			break;
		case 2:
			//2 * Sqrt(2) Pixels.
			state.period = 0x000000B5;
			break;
		case 3:
			//Reserved.
			break;
		}
		
		switch(phase) {
		case 0:
			state.phase = 0;
			break;
		case 1:
			// 1/4 of Period.
			state.phase = 0x00000010;
			break;
		case 2:
			// 2/4 of Period.
			state.phase = 0x00000020;
			break;
		case 3:
			// 3/4 of Period.
			state.phase = 0x00000030;
			break;
		}
		
		switch(threshold) {
		case 0:
			state.threshold = -1;
			break;
		case 1:
			//-3/8
			state.threshold = 0xFFFFFFE8;
			break;
		case 2:
			//-2/8
			state.threshold = 0xFFFFFFF0;
			break;
		case 3:
			//-1/8
			state.threshold = 0xFFFFFFF8;
			break;
		case 4:
			//0
			state.threshold = 0;
			break;
		case 5:
			//1/8
			state.threshold = 0x00000008;
			break;
		case 6:
			//2/8
			state.threshold = 0x00000010;
			break;
		case 7:
			//3/8
			state.threshold = 0x00000018;
			break;
		case 8:
			//4/8
			state.threshold = 0x00000020;
			break;
		case 9:
			//5/8
			state.threshold = 0x00000028;
			break;
		case 10:
			//6/8
			state.threshold = 0x00000030;
			break;
		case 11:
			//7/8
			state.threshold = 0x00000038;
			break;
		case 12:
			//8/8 = period.
			state.threshold = 0x00000040;
			break;
		case 13:
			//9/8
			state.threshold = 0x00000048;
			break;
		case 14:
			//10/8
			state.threshold = 0x00000050;
			break;
		case 15:
			//11/8
			state.threshold = 0x00000058;
			break;
		}
	} 
	
	/*
	 * Set the round state with fine control.
	 */
	public void superRound() {
		int n = ((Number) state.pop()).intValue();
		int period = (n >> 6) & 3;
		int phase = (n >> 4) & 3;
		int threshold = n & 15;
		
		//The period is determined in the first 2 pixels of the final byte.
		switch(period) {
		case 0:
			//1/2 Pixels.
			state.period = 0x00000020;
			break;
		case 1: 
			//1 Pixels.
			state.period = 0x00000040;
			break;
		case 2:
			//2 Pixels.
			state.period = 0x00000080;
			break;
		case 3:
			//Reserved.
			break;
		}
		
		switch(phase) {
		case 0:
			state.phase = 0;
			break;
		case 1:
			// 1/4 of Period.
			state.phase = 0x00000010;
			break;
		case 2:
			// 2/4 of Period.
			state.phase = 0x00000020;
			break;
		case 3:
			// 3/4 of Period.
			state.phase = 0x00000030;
			break;
		}
		
		switch(threshold) {
		case 0:
			state.threshold = -1;
			break;
		case 1:
			//-3/8
			state.threshold = 0xFFFFFFE8;
			break;
		case 2:
			//-2/8
			state.threshold = 0xFFFFFFF0;
			break;
		case 3:
			//-1/8
			state.threshold = 0xFFFFFFF8;
			break;
		case 4:
			//0
			state.threshold = 0;
			break;
		case 5:
			//1/8
			state.threshold = 0x00000008;
			break;
		case 6:
			//2/8
			state.threshold = 0x00000010;
			break;
		case 7:
			//3/8
			state.threshold = 0x00000018;
			break;
		case 8:
			//4/8
			state.threshold = 0x00000020;
			break;
		case 9:
			//5/8
			state.threshold = 0x00000028;
			break;
		case 10:
			//6/8
			state.threshold = 0x00000030;
			break;
		case 11:
			//7/8
			state.threshold = 0x00000038;
			break;
		case 12:
			//8/8 = period.
			state.threshold = 0x00000040;
			break;
		case 13:
			//9/8
			state.threshold = 0x00000048;
			break;
		case 14:
			//10/8
			state.threshold = 0x00000050;
			break;
		case 15:
			//11/8
			state.threshold = 0x00000058;
			break;
		}
	} 
	
	/*
	 * Round a number using the current round state.
	 */
	public int round(int n) {
		if(state.roundingOn) {
			//The actual threshold and phase.
			int threshold = (state.period * state.threshold) >> 6;
			int phase = (state.period * state.phase) >> 6;
			if(state.threshold == -1) {
				threshold = state.period - 1;
			}
			//The portion of the number to be rounded
			int unrounded = n%state.period;
			int rounded = 0;
			if(unrounded <  state.period - (threshold - phase)) {
				rounded = state.phase;
			}
			else if (unrounded >= state.period - (threshold - phase)) {
				rounded = phase + state.period;
			}
			//Return the number with the unrounded portion replaced with the rounded portion.
			return n - unrounded + rounded;
		}
		else {
			return n;
		}
	}
	
	/*
	 * Pop a number, round it, and then push it again.
	 */
	public void roundN(int ab) {
		//Ignore engine compensation for now.
		//Push the rounded version of the top stack element.
		state.push((round(((Number) state.pop()).intValue())), 6);
	}
	
	/*
	 * Only compensates for the engine characteristics.
	 */
	public void nRound(int ab) {
		//No compensation support yet.
	}
	
	/*
	 * Set the freedom vector to x-axis or y-axis.
	 */
	public void setFreedomVectorToAxis(int xy) {
		switch(xy) {
		case 0:
			//Set the freedom vector to the vertical axis.
			state.fv = new PointF2Dot14((short) 0, (short) 0xCFFF);
			break;
		case 1:
			//Set the freedom vector to the horizontal axis.
			state.fv = new PointF2Dot14((short) 0xCFFF, (short) 0);
			break;
		}
	}
	
	public void setFreedomVectorFromStack() {
		short x = ((Number) state.pop()).shortValue();
		short y = ((Number) state.pop()).shortValue();
		state.fv = new PointF2Dot14(x, y);
	}
	
	public void setFreedomVectorToLine(int var) {
		int c1 = ((Number) state.pop()).intValue();
		int c2 = ((Number) state.pop()).intValue();
		//Reference Points
		PointF26Dot6 p1 = new PointF26Dot6();
		PointF26Dot6 p2 = new PointF26Dot6();
		p1 = state.zone[state.zp1][c1];
		p2 = state.zone[state.zp1][c2];
		
		int deltaX = p1.x - p2.x;
		int deltaY = p1.y - p2.y;
		int sum = deltaX + deltaY;
		
		int factor = 0;
		switch(var) {
		case 0:
			factor = (deltaX << 14)/sum;
			break;
		case 1:
			factor = (deltaY << 14)/sum;
			break;
		}
		short resX = (short) (((int) (factor * 0x0000C000) >> 14)&0x0FFFFFFF);
		short resY = (short) (((int) (factor * 0x0000C000) >> 14)&0x0FFFFFFF);
		
		state.fv = new PointF2Dot14(resX, resY);
	}
	
	public void setProjectionVectorToAxis(int xy) {
		switch(xy) {
		case 0:
			state.pv = new PointF2Dot14((short) 0, (short) 0xCFFF);
			break;
		case 1:
			state.pv = new PointF2Dot14((short) 0xCFFF, (short) 0);
			break;
		}
	}
	
	public void setProjectionVectorFromStack() {
		short x = ((Number) state.pop()).shortValue();
		short y = ((Number) state.pop()).shortValue();
		state.pv = new PointF2Dot14(x, y);
	}
	
	public void setProjectionVectorToLine(int var) {
		int c1 = ((Number) state.pop()).intValue();
		int c2 = ((Number) state.pop()).intValue();
		//Reference Points
		PointF26Dot6 p1 = new PointF26Dot6();
		PointF26Dot6 p2 = new PointF26Dot6();
		p1 = state.zone[state.zp1][c1];
		p2 = state.zone[state.zp1][c2];
		
		int deltaX = p1.x - p2.x;
		int deltaY = p1.y - p2.y;
		int sum = deltaX + deltaY;
		
		int factor = 0;
		switch(var) {
		case 0:
			factor = (deltaX << 14)/sum;
			break;
		case 1:
			factor = (deltaY << 14)/sum;
			break;
		}
		short resX = (short) (((int) (factor * 0x0000C000) >> 14)&0x0FFFFFFF);
		short resY = (short) (((int) (factor * 0x0000C000) >> 14)&0x0FFFFFFF);
		
		state.pv = new PointF2Dot14(resX, resY);
	}
	
	public void setDualProjectionVectorToLine(int var) {
		int c1 = ((Number) state.pop()).intValue();
		int c2 = ((Number) state.pop()).intValue();
		//Set the projection vector parallel to the two points in zone1.
		//Reference Points
		PointF26Dot6 p1 = new PointF26Dot6();
		PointF26Dot6 p2 = new PointF26Dot6();
		p1 = state.zone[state.zp1][c1];
		p2 = state.zone[state.zp2][c2];
		
		int deltaX = p1.x - p2.x;
		int deltaY = p1.y - p2.y;
		int sum = deltaX + deltaY;
		
		int factor = 0;
		switch(var) {
		case 0:
			factor = (deltaX << 14)/sum;
			break;
		case 1:
			factor = (deltaY << 14)/sum;
			break;
		}
		short resX = (short) (((int) (factor * 0x0000C000) >> 14)&0x0FFFFFFF);
		short resY = (short) (((int) (factor * 0x0000C000) >> 14)&0x0FFFFFFF);
		
		state.pv = new PointF2Dot14(resX, resY);
		
		//Set the dual projection vector parallel to the two points in the original outline.
		PointF26Dot6 d1 = new PointF26Dot6();
		PointF26Dot6 d2 = new PointF26Dot6();
		d1 = state.original[state.zp1][c1];
		d2 = state.original[state.zp2][c2];
		
		deltaX = d1.x - d2.x;
		deltaY = d1.y - d2.y;
		sum = deltaX + deltaY;
		
		factor = 0;
		switch(var) {
		case 0:
			factor = (deltaX << 14)/sum;
			break;
		case 1:
			factor = (deltaY << 14)/sum;
			break;
		}
		resX = (short) (((int) (factor * 0x0000C000) >> 14)&0x0FFFFFFF);
		resY = (short) (((int) (factor * 0x0000C000) >> 14)&0x0FFFFFFF);
		
		state.dpv = new PointF2Dot14(resX, resY);
	}
	
	public void setFreedomVectorToProjectionVector() {
		state.fv = state.pv;
	}
	
	public void setVectorsToAxis(int xy) {
		switch(xy) {
		case 0:
			state.fv = new PointF2Dot14((short) 0, (short) 0xCFFF);
			state.pv = new PointF2Dot14((short) 0, (short) 0xCFFF);
			break;
		case 1:
			state.fv = new PointF2Dot14((short) 0xCFFF, (short) 0);
			state.pv = new PointF2Dot14((short) 0xCFFF, (short) 0);
			break;
		}
	}
	
	public void getFreedomVector() {
		state.push(state.fv.y, 3);
		state.push(state.fv.x, 3);
	}
	
	public void getProjectionVector() {
		state.push(state.pv.y, 3);
		state.push(state.pv.x, 3);
	}
	
	/*
	 * Find the location a point falls along the projection vector.
	 */
	public int getCoordinateOnProjection(PointF26Dot6 p) {
		int slope = 1;
		int slope2 = -1;
		try {
			//The slope of the projection vector.
			slope =  (((int) state.pv.y << 14)/state.pv.x)>>8;
		}
		catch(ArithmeticException e) {
			return p.y;
		}
		try {
			//The inverse slope.
			slope2 = -((1<<14)/slope)>>8;
		}
		catch(ArithmeticException e) {
			return p.x;
		}
		
		//The y intercept of the inverse line.
		//y=mx+b -> b=y-mx
		int b = -((slope2*p.x)>>6) + p.y; 
		
		//The intersection x of the two lines.
		int x = ((b) << 6)/(slope - slope2);
		
		//The resulting distance. Uses triangle ratios to be determined.
		int res = ((x << 14)/state.pv.x);
		return res;
	}
	
	/*
	 * Find the location a point falls along the dual projection vector.
	 */
	public int getCoordinateOnDualProjection(PointF26Dot6 p) {
		int slope = 1;
		int slope2 = -1;
		try {
			//The slope of the projection vector.
			slope =  (((int) state.dpv.y << 14)/state.dpv.x)>>8;
		}
		catch(ArithmeticException e) {
			return p.y;
		}
		try {
			//The inverse slope.
			slope2 = -((1<<14)/slope)>>8;
		}
		catch(ArithmeticException e) {
			return p.x;
		}
		
		//The y intercept of the inverse line.
		//y=mx+b -> b=y-mx
		int b = -((slope2*p.x)>>6) + p.y; 
		
		//The intersection x of the two lines.
		int x = ((b) << 6)/(slope - slope2);
		
		//The resulting distance. Uses triangle ratios to be determined.
		int res = ((x << 14)/state.dpv.x);
		return res;
	}
	
	public PointF26Dot6 getIntersection(PointF26Dot6 p, int projectionCoord) {
		PointF26Dot6 res = new PointF26Dot6();
		int freedomSlope;
		int freedomB;
		int projectionSlope;
		int inverseProjectionSlope;
		int inverseProjectionB;
		try {
			freedomSlope = (((int) state.fv.y << 14)/state.fv.x)>>8;
			freedomB = -((freedomSlope*p.x)>>6) + p.y; 
			try {
				//No Exceptions Block
				//The slope of the projection vector.
				projectionSlope =  (((int) state.pv.y << 14)/state.pv.x)>>8;
			}
			catch(ArithmeticException e) {
				//Projection Vector is vertical, inverse is horizontal.
				if(freedomSlope == 0) {
					//The vectors are perpendicular. Illegal.
					res.x = 0;
					res.y = 0;
					return res;
				}
				res.y = projectionCoord;
				res.x = ((res.y-freedomB)<<6)/freedomSlope;
				return res;
			}
			try {
				//The inverse slope.
				inverseProjectionSlope = -((1<<14)/projectionSlope)>>8;
				inverseProjectionB = -((inverseProjectionSlope*p.x)>>6) + p.y; 
			}
			catch(ArithmeticException e) {
				//Projection Vector is horizontal, inverse is vertical.
				res.x = projectionCoord;
				res.y = ((freedomSlope * res.x)>>6)+freedomB;
				return res;
			}
			//Find the intersection normally.
			res.x = ((inverseProjectionB - freedomB) << 6)/(freedomSlope-projectionSlope);
			res.y = ((freedomSlope * res.x) >> 6) + freedomB;
			return res;
		}
		catch(ArithmeticException e) {
			try {
				//The slope of the projection vector.
				projectionSlope =  (((int) state.pv.y << 14)/state.pv.x)>>8;
			}
			catch(ArithmeticException b) {
				//Projection Vector is vertical, inverse is horizontal.
				res.y = projectionCoord;
				res.x = p.x;
				return res;
			}
			try {
				//The inverse slope.
				inverseProjectionSlope = -((1<<14)/projectionSlope)>>8;
				inverseProjectionB = -((inverseProjectionSlope*p.x)>>6) + p.y; 
			}
			catch(ArithmeticException b) {
				//The vectors are perpendicular. Illegal.
				res.x = 0;
				res.y = 0;
				return res;
			}
			//Freedom vector is vertical, inverse projection vector is normal.
			res.x = p.x;
			res.y = ((inverseProjectionSlope * res.x) >> 6) + inverseProjectionB;
			return res;
		}	
	}
	
	/*
	 * Push the coordinate of a point along the projection vector onto the stack.
	 */
	public void getCoordinate(int val) {
		int pointNumber = ((Number) state.pop()).intValue();
		PointF26Dot6 p = new PointF26Dot6();
		//Use the correct zone.
		switch(val) {
		case 0:
			p = state.zone[state.zp2][pointNumber];
			break;
		case 1:
			p = state.original[state.zp2][pointNumber];
			break;
		}
		
		//Find the coordinate and push.
		state.push(getCoordinateOnProjection(p), 6);
	}
	
	/*
	 * Set the coordinate to a specified position along the projection vector.
	 */
	public void setCoordinateFromStack() {
		int c = ((Number) state.pop()).intValue();
		int pointNumber = ((Number) state.pop()).intValue();
		
		PointF26Dot6 p = new PointF26Dot6();
		
		//Get the point.
		p = state.zone[state.zp2][pointNumber];
		p = getIntersection(p, c);
		
		state.zone[state.zp2][pointNumber] = p;
	}
	
	/*
	 * Push onto the stack the distance along the projection vector between two points.
	 */
	public void measureDistance(int a) {
		int n1 = ((Number) state.pop()).intValue();
		int n2 = ((Number) state.pop()).intValue();
		PointF26Dot6 p1 = new PointF26Dot6();
		PointF26Dot6 p2 = new PointF26Dot6();
		switch(a) {
		case 0:
			//Measure the distance on the grid fitted outline using the projection vector.
			p1 = state.zone[state.zp0][n1];
			p2 = state.zone[state.zp1][n2];
			state.push(measure(p1, p2, false), 6);
			break;
		case 1:
			//Measure the distance on the original outline using the dual projection vector.
			p1 = state.original[state.zp0][n1];
			p2 = state.original[state.zp1][n2];
			state.push(measure(p1, p2, true), 6);
			break;
		}
	}
	
	/*
	 * Measure the distance along the projection vector between two points.
	 */
	public int measure(PointF26Dot6 p1, PointF26Dot6 p2, boolean vector) {
		if(vector == false) {
			//Measure the distance on the grid fitted outline using the projection vector.
			return getCoordinateOnProjection(p1) - getCoordinateOnProjection(p2);
		}
		else {
			//Use the dual projection vector.
			return getCoordinateOnDualProjection(p1) - getCoordinateOnDualProjection(p2);
		}
	}
	
	/*
	 * Shift a series of points a distance along the freedom vector set by the distance a reference point has been moved.
	 */
	public void shiftPoints(int a) {
		//Get the reference point.
		int rp;
		PointF26Dot6 initialPoint, finalPoint;
		if(a==1) {
			//Use rp1 in the zone pointed to by zp0.
			rp = state.rp[1];
			initialPoint = state.original[state.zp0][rp];
			finalPoint = state.zone[state.zp0][rp];
		}
		else {
			//Use rp2 in the zone pointed to by zp1.
			rp = state.rp[2];
			initialPoint = state.original[state.zp1][rp];
			finalPoint = state.zone[state.zp1][rp];
		}
		//Find the distance the reference point has been moved.
		int distance = measure(initialPoint, finalPoint, false);
		for(int i = 0; i < state.loop; i++) {
			//The point to be shifted.
			int num = ((Number) state.pop()).intValue();
			PointF26Dot6 p = state.zone[state.zp2][num];
			//Find and set the location of the shifted point.
			PointF26Dot6 newPoint = getIntersection(p, distance);
			state.zone[state.zp2][num] = newPoint;
		}
	}
	
	/*
	 * Shift an entire a zone a distance.
	 */
	public void shiftZone(int a) {
		//Get the reference point.
		int rp;
		PointF26Dot6 initialPoint, finalPoint;
		if(a==1) {
			//Use rp1 in the zone pointed to by zp0.
			rp = state.rp[1];
			initialPoint = state.original[state.zp0][rp];
			finalPoint = state.zone[state.zp0][rp];
		}
		else {
			//Use rp2 in the zone pointed to by zp1.
			rp = state.rp[2];
			initialPoint = state.original[state.zp1][rp];
			finalPoint = state.zone[state.zp1][rp];
		}
		//Find the distance the reference point has been moved.
		int distance = measure(initialPoint, finalPoint, false);
		//Find the zone to be shifted.
		int e = ((Number) state.pop()).intValue();
		for(int i = 0; i < state.zone[e].length; i++) {
			//The point to be shifted.
			PointF26Dot6 p = state.zone[e][i];
			//Find and set the location of the shifted point.
			PointF26Dot6 newPoint = getIntersection(p, distance);
			state.zone[e][i] = newPoint;
		}
	}
	
	/*
	 * Shift an entire contour a distance determined by the reference point.
	 */
	public void shiftContour(int a) {
		//Get the reference point.
		int rp;
		PointF26Dot6 initialPoint, finalPoint;
		if(a==1) {
			//Use rp1 in the zone pointed to by zp0.
			rp = state.rp[1];
			initialPoint = state.original[state.zp0][rp];
			finalPoint = state.zone[state.zp0][rp];
		}
		else {
			//Use rp2 in the zone pointed to by zp1.
			rp = state.rp[2];
			initialPoint = state.original[state.zp1][rp];
			finalPoint = state.zone[state.zp1][rp];
		}
		//Find the distance the reference point has been moved.
		int distance = measure(initialPoint, finalPoint, false);
		//Find the contour to be shifted.
		int c = ((Number) state.pop()).intValue();
		for(int i = table.contourEnds[c-1]+1; i <= table.contourEnds[c]; i++) {
			//The point to be shifted.
			PointF26Dot6 p = state.zone[state.zp2][i];
			//Find and set the location of the shifted point.
			PointF26Dot6 newPoint = getIntersection(p, distance);
			state.zone[state.zp2][i] = newPoint;
		}
	}

	/*
	 * Shift a point along the freedom vector by a pixel value.
	 */
	public void shiftPointByPixel() {
		//The distance to be shifted along the freedom vector.
		int d = ((Number) state.pop()).intValue();
		for(int i = 0; i < state.loop; i++) {
			//The point to be shifted.
			int num = ((Number) state.pop()).intValue();
			PointF26Dot6 p = state.zone[state.zp2][num];
			
			//Convert 2.14 vector nums to 26.6 point nums.
			int deltaX = state.fv.x >> 8;
			int deltaY = state.fv.y >> 8;
			deltaX *= d;
			deltaY *= d;
			
			p.x += deltaX;
			p.y += deltaY;
			
			state.zone[state.zp2][num] = p;
		}
	}
	
	/*
	 * Set the control value table cut in value in the graphics state. 
	 */
	public void setCVTCutIn() {
		int cutIn = ((Number) state.pop()).intValue();
		state.cutIn = cutIn;
	}
	
	/*
	 * Move a point to an absolute location along the projection vector.
	 */
	public void moveIndirectAbsolutePoint(int a) {		
		int cvt = ((Number) state.pop()).intValue();
		int p = ((Number) state.pop()).intValue();
		
		int coord = cvtTable.controlValues[cvt];
		
		switch(a) {
		case 0:
			//Don't round the number and don't look at the control value cut in.
			break;
		case 1:
			//The distance between the CVT value and the position.
			int distance = coord - getCoordinateOnProjection(state.zone[state.zp0][p]);
			//If the distance is greater than the cut in value, round it.
			if(Math.abs(distance) > state.cutIn) {
				coord = round(getCoordinateOnProjection(state.zone[state.zp0][p]));
			}
			else {
				//The control value is used.
				coord = round(coord);
			}
			break;
		}
		//Move the point.
		PointF26Dot6 newPoint = getIntersection(state.zone[state.zp0][p], coord);
		state.zone[state.zp0][p] = newPoint;
		
		//Set the reference points to p.
		state.rp[0] = p;
		state.rp[1] = p;
	}
	
	/*
	 * Move direct absolute point. I have no idea what the fuck this does either.
	 */
	public void moveDirectAbsolutePoint(int a) {
		int p = ((Number) state.pop()).intValue();
		
		switch(a) {
		case 0:
			break;
		case 1:
			//Move the point.
			int delta = round(getCoordinateOnProjection(state.zone[state.zp0][p]));
			state.zone[state.zp0][p] = getIntersection(state.zone[state.zp0][p],  delta);
			break;
		}
		//Dap the point. Depending on the projection vector, the point can be "touched" on the x or y axis.
		if(state.pv.x != 0) {
			state.zone[state.zp0][p].touchX = true;
		}
		if(state.pv.y != 0) {
			state.zone[state.zp0][p].touchY = true;
		}
		
		//Set the reference points to p.
		state.rp[0] = p;
		state.rp[1] = p;
	}
	
	/*
	 * Move a point a distance relative to a reference point that has been moved.
	 */
	public void moveDirectRelativePoint(int a, int b, int c, int de) {
		int p = ((Number) state.pop()).intValue();
		
		//The original distance between the reference point and the point.
		int originalDistance = measure(state.original[state.zp1][p], state.original[state.zp0][state.rp[0]], true);
		
		//Check the Single Width Cut In.
		//If the distance is close enough to the single width, just use the single width instead.
		if(originalDistance - state.singleWidth < state.singleWidthCutIn) {
			originalDistance = state.singleWidth;
		}
		
		//Compensate for the color of the distance.
		//We'll ignore this for now.
		
		//Round the distance.
		switch(c) {
		case 0:
			//Do not round the distance.
			break;
		case 1:
			//Round the distance.
			originalDistance = round(originalDistance);
			break;
		}
		
		//Check the minimum distance.
		switch(b) {
		case 0:
			//Do not keep distance greater than or equal to minimum distance.
			break;
		case 1:
			//Keep distance greater than or equal to minimum distance.
			if(originalDistance < state.minDistance) {
				originalDistance = state.minDistance;
			}
			break;
		}
		
		//Move the point.	
		//The location of the reference point on the projection vector.
		int referenceCoordinate = getCoordinateOnProjection(state.original[state.zp0][state.rp[0]]);
		//The new coordinate of point p.
		PointF26Dot6 newCoordinate = getIntersection(state.original[state.zp1][p], originalDistance + referenceCoordinate);
		state.original[state.zp1][p] = newCoordinate;
		//Reset rp1 and rp2.
		state.rp[1] = state.rp[0];
		state.rp[2] = p;
		//Reset reference point?
		switch(a) {
		case 0:
			//Do not reset rp0 to point p.
			break;
		case 1:
			//Set rp0 to point p.
			state.rp[0] = p;
			break;
		}
	}
	
	/*
	 * Move Indirect Relative Point. This is confusing!!!!.
	 */
	public void moveIndirectRelativePoint(int a, int b, int c, int de) {
		int cvt = ((Number) state.pop()).intValue();
		int p = ((Number) state.pop()).intValue();
		
		//The distance in the cvt table.
		int cvDistance = cvtTable.controlValues[cvt];
		
		//The original distance between the reference point and the point.
		int originalDistance = measure(state.original[state.zp1][p], state.original[state.zp0][state.rp[0]], true);
				
		//Check Single Width Cut In.
		//If the distance is close enough to the single width, just use the single width instead.
		if(cvDistance - state.singleWidth < state.singleWidthCutIn) {
			cvDistance = state.singleWidth;
		}
		
		int distance = 0;
		switch(c) {
		case 0:
			//Do not round the distance and do not look at the control value cut in.
			break;
		case 1:
			//Round the distance look at the control value cut in.
			//Check Control Value Cut In.
			//Check Control Value Cut In.
			if(Math.abs(originalDistance) - cvDistance > state.cutIn) {
				//Compensate for the Color of the Distance.
				//We'll skip this for now.
				
				//The original distance is used.
				distance = round(originalDistance);
			}
			else {
				//Compensate for the Color of the Distance.
				//We'll skip this for now.
				
				//The CVT value will be used.
				distance = round(cvDistance);
			}
			break;
		}
		//Check the Minimum Distance.
		switch(b) {
		case 0:
			//Do not keep distance greater than or equal to minimum distance.
			break;
		case 1:
			//Keep distance greater than or equal to minimum distance.
			if(distance < state.minDistance) {
				distance = state.minDistance;
			}
			break;
		}
		//Move the Point.
		//The location of the new coordinate on the projection vector. The distance plus the location of the reference point.
		int newCoordinate = getCoordinateOnProjection(state.zone[state.zp0][state.rp[0]]) + distance;
		//Set the new location of the point.
		state.original[state.zp1][p] = getIntersection(state.zone[state.zp1][p], newCoordinate);
		//Reset rp1 and rp2.
		state.rp[1] = state.rp[0];
		state.rp[2] = p;
		//Reset reference point?
		switch(a) {
		case 0:
			//Do not reset rp0 to point p.
			break;
		case 1:
			//Set rp0 to point p.
			state.rp[0] = p;
			break;
		}
	}
	
	/*
	 * Move the point to a location relative to a reference point using a distance from the stack.
	 */
	public void moveStackIndirectRelativePoint(int a) {
		int distance = ((Number) state.pop()).intValue();
		int p = ((Number) state.pop()).intValue();
		
		//Move the Point.
		//The location of the new coordinate on the projection vector. The reference point plus the distance.
		int newCoordinate = getCoordinateOnProjection(state.zone[state.zp0][state.rp[0]]) + distance;
		//Set the new location of the point.
		state.original[state.zp1][p] = getIntersection(state.zone[state.zp1][p], newCoordinate);
		//Reset rp1 and rp2.
		state.rp[1] = state.rp[0];
		state.rp[2] = p;
		//Reset reference point?
		switch(a) {
		case 0:
			//Do not reset rp0 to point p.
			break;
		case 1:
			//Set rp0 to point p.
			state.rp[0] = p;
			break;
		}
	}
	
	/*
	 * Align two points to the same position along the projection vector, at the midpoint of their positions.
	 */
	public void alignPoints() {
		int p2 = ((Number) state.pop()).intValue();
		int p1 = ((Number) state.pop()).intValue();
		
		//The distance from each point to the midpoint between them.
		int distance = measure(state.zone[state.zp1][p1], state.zone[state.zp0][p2], false)/2;
		
		//Get p1 coordinate.
		int p1New = getCoordinateOnProjection(state.zone[state.zp1][p1]) - distance;
		//Shift p1.
		state.zone[state.zp1][p1] = getIntersection(state.zone[state.zp1][p1], p1New);
		//Get p2 coordinate.
		int p2New = getCoordinateOnProjection(state.zone[state.zp0][p2]) + distance;
		//Shift p2.
		state.zone[state.zp0][p2] = getIntersection(state.zone[state.zp0][p2], p2New);
	}
	
	/*
	 * Align a series of points to a reference point.
	 */
	public void alignReferencePoint() {
		//Find the coordinate that the reference point falls along the projection vector.
		int coordinate = getCoordinateOnProjection(state.zone[state.zp0][state.rp[0]]);
		for(int i = 0; i < state.loop; i++) {
			int pointNum = ((Number) state.pop()).intValue();
			//Align it to the coordinate location of the reference point.
			state.zone[state.zp1][pointNum] = getIntersection(state.zone[state.zp1][pointNum], coordinate);
		}
	}
	
	/*
	 * Move a point to the intersection of two lines.
	 */
	public void movePointToIntersection() {
		PointF26Dot6 a0 = state.zone[state.zp0][((Number) state.pop()).intValue()];
		PointF26Dot6 a1 = state.zone[state.zp0][((Number) state.pop()).intValue()];
		PointF26Dot6 b0 = state.zone[state.zp1][((Number) state.pop()).intValue()];
		PointF26Dot6 b1 = state.zone[state.zp1][((Number) state.pop()).intValue()];
		int p = ((Number) state.pop()).intValue();
		
		state.zone[state.zp2][p] = getIntersectionLine(a0, a1, b0, b1);
	}
	
	/*
	 * Get the intersection of two lines.
	 */
	public PointF26Dot6 getIntersectionLine(PointF26Dot6 a0, PointF26Dot6 a1, PointF26Dot6 b0, PointF26Dot6 b1) {
		int slopeA, slopeB;
		int interceptA, interceptB;
		int x, y;
		
		try {
			slopeA = ((a0.y - a1.y) << 6)/(a0.x - a1.x);
			interceptA = a0.y - ((slopeA * a0.x) >> 6);
		}
		catch(ArithmeticException e) {
			int invertSlopeB;
			interceptA = a0.x;
			try {
				invertSlopeB = ((b0.x - b1.x) << 6)/(b0.y - b1.y);
				interceptB = b0.x - ((invertSlopeB * b0.y) >> 6);
			}
			catch(ArithmeticException b) {
				//The two lines are parallel.
				//Find the point in the middle of the parallel lines.
				x = ((((a0.x + a1.x) << 6)/0x00000080) + ((((b0.x + b1.x) << 6)/0x00000080)) << 6)/0x00000080;
				y = ((((a0.y + a1.y) << 6)/0x00000080) + ((((b0.y + b1.y) << 6)/0x00000080)) << 6)/0x00000080;
				return new PointF26Dot6(x, y);
			}
			y = ((interceptB - interceptA) << 6)/(-invertSlopeB);
			x = interceptA;
			return new PointF26Dot6(x, y);
		}
		try {
			slopeB = ((b0.y - b1.y) << 6)/(b0.x - b1.x);
			interceptB = b0.y - ((slopeB * b0.x) >> 6);
		}
		catch(ArithmeticException e) {
			int invertSlopeA = ((a0.x - a1.x) << 6)/(a0.y - a1.y);
			interceptA = a0.x - ((invertSlopeA * a0.y) >> 6);
			interceptB = b0.x;
			
			y = ((interceptA - interceptB) << 6)/(-invertSlopeA);
			x = interceptB;
			return new PointF26Dot6(x, y);
		}

		try {
			//Find the intersection of the two lines.
			x = ((interceptB - interceptA) << 6)/(slopeA - slopeB);
			y = ((slopeA * x) >> 6) + interceptA;
			return new PointF26Dot6(x, y);
		}
		catch(ArithmeticException e) {
			//The two lines are parallel.
			//Find the point in the middle of the parallel lines.
			x = ((((a0.x + a1.x) << 6)/0x00000080) + ((((b0.x + b1.x) << 6)/0x00000080)) << 6)/0x00000080;
			y = ((((a0.y + a1.y) << 6)/0x00000080) + ((((b0.y + b1.y) << 6)/0x00000080)) << 6)/0x00000080;
			return new PointF26Dot6(x, y);
		}
	}
	
	/*
	 * Untouches a point depending on the freedom vector.
	 */
	public void untouchPoint() {
		int p = ((Number) state.pop()).intValue();
		
		//If the vector is set to the x-axis, the point will be untouched in the x-direction. 
		//If the vector is set to the y-axis, the point will be untouched in the y-direction. 
		//Otherwise the point will be untouched in both directions.
		if(state.fv.x != 0) {
			state.zone[state.zp0][p].touchX = false;
		}
		if(state.fv.y != 0) {
			state.zone[state.zp0][p].touchY = false;
		}
 	}
	
	/*
	 * Interpolates the positions of the specified points to preserve the relationship between two reference points.
	 */
	public void interpolatePoint() {
		//The reference points in the original outline and in the instructed one.
		PointF26Dot6 rp1i = state.original[state.zp0][state.rp[1]];
		PointF26Dot6 rp2i = state.original[state.zp1][state.rp[2]];
		PointF26Dot6 rp1 = state.zone[state.zp0][state.rp[1]];
		PointF26Dot6 rp2 = state.zone[state.zp1][state.rp[2]];
		//Iterate through the set of points.
		for(int i = 0; i < state.loop; i++) {
			int p = ((Number) state.pop()).intValue();
			//The original and current location of the point being moved.
			PointF26Dot6 originalPoint = state.original[state.zp2][p];
			PointF26Dot6 point = state.zone[state.zp2][p];
			
			//The distances from the two reference points.
			int d1 = measure(originalPoint, rp1i, true);
			int d2 = measure(originalPoint, rp2i, true);
			
			//The total distance between the two reference points currently.
			int d = measure(rp1, rp2, false);
			
			//The distance from rp1 to p.
			int d3 = ((d1 << 6)/(d1 + d2)) * d;
			
			//Set the new location of the point.
			PointF26Dot6 newPoint = getIntersection(point, getCoordinateOnProjection(rp1) + d3);
			state.zone[state.zp2][p] = newPoint;
		}
	}
	
	/*
	 * Interpolate all the untouched points along one direction.
	 */
	public void interpolateUntouchedPoints(int a) {
		PointF26Dot6 prev, p, next;
		switch(a) {
		case 0:
			prev = state.zone[state.zp2][state.zone[state.zp2].length];
			//Go through the entire glyph.
			for(int i = 0; i < state.zone[state.zp2].length; i++) {
				p = state.zone[state.zp2][i];
				try {
					next = state.zone[state.zp2][i+1];
				}
				//For the final iteration next is the first element.
				catch(IndexOutOfBoundsException e) {
					next = state.zone[state.zp2][0];
				}
				//If an untouched point is between two touched points.
				if(!p.touchX && prev.touchX && next.touchX) {
					//The original distances to the two touched points from p.
					int d1 = state.original[state.zp2][i].x - state.original[state.zp2][i-1].x;
					int d2 = state.original[state.zp2][i].x - state.original[state.zp2][i+1].x;
					//If the point is not between the two touched points.
					if((d1 >= 0 && d2 >= 0) || (d1 <= 0 && d2 <= 0)) {
						//Shift the point the ammount the nearest touched point was shifted from its original position.
						//Find the nearest point and find how much it has been shifted on the dual projection vector.
						if(Math.abs(d1) <= Math.abs(d2)) {
							int d = prev.x - state.original[state.zp2][i-1].x;
							state.zone[state.zp2][i].x += d;
						}
						else {
							int d = next.x - state.original[state.zp2][i-1].x;
							state.zone[state.zp2][i].x += d;
						}
					}
					else {
						//Linearly interpolate the point in its current position using the original distances.
						//The distance between the two touched points currently.
						int d = prev.x - next.x;
						
						//Distance from prev to interpolated point.
						int d3 = ((d1 << 6)/(d1 + d2)) * d;
						//Set the new location of the point.
						state.zone[state.zp2][i].x += d3;
					}
					//Skip one iteration (because p will not be untouched).
					i++;
					prev = next;
				}
				else {
					//Check the next set of points.
					prev = p;
				}
			}
			break;
		case 1:
			prev = state.zone[state.zp2][state.zone[state.zp2].length];
			//Go through the entire glyph.
			for(int i = 0; i < state.zone[state.zp2].length; i++) {
				p = state.zone[state.zp2][i];
				try {
					next = state.zone[state.zp2][i+1];
				}
				//For the final iteration next is the first element.
				catch(IndexOutOfBoundsException e) {
					next = state.zone[state.zp2][0];
				}
				//If an untouched point is between two touched points.
				if(!p.touchY && prev.touchY && next.touchY) {
					//The original distances to the two touched points from p.
					int d1 = state.original[state.zp2][i].y - state.original[state.zp2][i-1].y;
					int d2 = state.original[state.zp2][i].y - state.original[state.zp2][i+1].y;
					//If the point is not between the two touched points.
					if((d1 >= 0 && d2 >= 0) || (d1 <= 0 && d2 <= 0)) {
						//Shift the point the ammount the nearest touched point was shifted from its original position.
						//Find the nearest point and find how much it has been shifted on the dual projection vector.
						if(Math.abs(d1) <= Math.abs(d2)) {
							int d = prev.y - state.original[state.zp2][i-1].y;
							state.zone[state.zp2][i].y += d;
						}
						else {
							int d = next.y - state.original[state.zp2][i-1].y;
							state.zone[state.zp2][i].y += d;
						}
					}
					else {
						//Linearly interpolate the point in its current position using the original distances.
						//The distance between the two touched points currently.
						int d = prev.y - next.y;
						
						//Distance from prev to interpolated point.
						int d3 = ((d1 << 6)/(d1 + d2)) * d;
						//Set the new location of the point.
						state.zone[state.zp2][i].y += d3;
					}
					//Skip one iteration (because p will not be untouched).
					i++;
					prev = next;
				}
				else {
					//Check the next set of points.
					prev = p;
				}
			}
			break;
		}
	}
	
	/*
	 * Exceptions at specific pixels per em.
	 */
	public void deltaP(int a) {
		//The ammount added to the delta base and relative pixel per em value.
		int add = 16 * (a-1);
		//The number of exceptions.
		int n = ((Number) state.pop()).intValue();
		for(int i = 0; i < n; i++) {
			int arg = ((Number) state.pop()).intValue();
			int p = ((Number) state.pop()).intValue();
			//The value of arg consists of a byte with lower four bits of which represent the magnitude of the exception
			//and the upper four bits, the relative pixel per em value.
			int stepNum = (arg & 0xF) - 8;
			int relativePixelsPerEM = arg >> 4;
			
			//If the correct pixels per em.
			if((state.deltaBase + add + relativePixelsPerEM) == pixelsPerEm) {
				//Find the new location of the point.
				PointF26Dot6 newPoint = getIntersection(state.zone[state.zp0][p], getCoordinateOnProjection(state.zone[state.zp0][p]) + (stepNum * state.deltaShift));
				state.zone[state.zp0][p] = newPoint;
			}
		}
	}
	
	/*
	 * Exceptions at specific pixels per em.
	 */
	public void deltaC(int a) {
		//The ammount added to the delta base and relative pixel per em value.
		int add = 16 * (a-1);
		//The number of exceptions.
		int n = ((Number) state.pop()).intValue();
		for(int i = 0; i < n; i++) {
			int arg = ((Number) state.pop()).intValue();
			int c = ((Number) state.pop()).intValue();
			//The value of arg consists of a byte with lower four bits of which represent the magnitude of the exception
			//and the upper four bits, the relative pixel per em value.
			int stepNum = (arg & 0xF) - 8;
			int relativePixelsPerEM = arg >> 4;
			
			//If the correct pixels per em.
			if((state.deltaBase + add + relativePixelsPerEM) == pixelsPerEm) {
				//Change the control value.
				font.cvt.controlValues[c] += (stepNum * state.deltaShift); 
			}
		}
	}
	
	/*
	 * Put some info onto the stack.
	 */
	public void getInfo() {
		int selector = ((Number) state.pop()).intValue();
		int res = 0;
		//Rotated?
		if(((selector>>2)&1) == 1 && rotated) {
			res += 256;
		}
		//Stretched?
		if(((selector>>1)&1) == 1 && stretched) {
			res += 128;
		}
		//Engine Version.
		if((selector&1) == 1) {
			//3, because apple sucks.
			res += 3;
		}
		state.push(res, 4);
	}

	/*
	 * Read an element from storage and push it onto the stack.
	 */
	public void readStore() {
		int n = ((Number) state.pop()).intValue();
		state.push(state.storage[n], 4);
	}
	
	/*
	 * Write an element to an index of storage.
	 */
	public void writeStore() {
		int v = ((Number) state.pop()).intValue();
		int l = ((Number) state.pop()).intValue();
		
		state.storage[l] = v;
	}
	
	/*
	 * Set the scan contol graphics state variable depending on a series of flags.
	 */
	public void scanControl() {
		int n = ((Number) state.pop()).intValue();
		
		int threshold = (n & 0xFF) + 1;
		int b8 = (n >> 8)&1;
		int b9 = (n >> 9)&1;
		int b10 = (n >> 10)&1;
		int b11 = (n >> 11)&1;
		int b12 = (n >> 12)&1;
		int b13 = (n >> 13)&1;
		
		if(pixelsPerEm <= threshold) {
			if((b12 == 1 && rotated) || (b13 == 1 && stretched)) {
				//Set dropout control to FALSE unless the glyph is rotated.
				//Set dropout control to FALSE unless the glyph is stretched
				state.scanControl = false;
			}
			else {
				if(b8 == 1) {
					//Set dropout control to TRUE if other conditions do not block 
					//and ppem is less than or equal to the threshold value
					state.scanControl = true;
				}
				if(b9 == 1 && rotated) {
					//Set dropout control to TRUE if other conditions do not block and the glyph is rotated
					state.scanControl = true;
				}
				if(b10 == 1 && stretched) {
					//Set dropout control to TRUE if other conditions do not block and the glyph is stretched.
					state.scanControl = true;
				}
			}
		}
		if(b11 == 1) {
			//Set dropout control to FALSE unless ppem is less than or equal to the threshold value.
			state.scanControl = false;
		}
		
	}
	
	/*
	 * Sets the rules for scan conversion.
	 */
	public void scanType() {
		scanType = ((Number) state.pop()).intValue();
	}
	
	/*
	 * Write an element to the control values table in pixel units.
	 */
	public void writeControlValueTablePixels() {
		int v = ((Number) state.pop()).intValue();
		int l = ((Number) state.pop()).intValue();
		
		font.cvt.controlValues[l] = v;
	}
	
	/*
	 * Write an element to the control values table in unscaled Funits.
	 */
	public void writeControlValueTableFunits() {
		int v = ((Number) state.pop()).intValue();
		int l = ((Number) state.pop()).intValue();
		
		int scale = ((pointSize * resolution) << 6)/(72 * font.head.unitsPerEm);
		
		font.cvt.controlValues[l] = (v * scale) >> 6;
	}
	
	/*
	 * Reads an entry from the CVT table.
	 */
	public void readControlValueTableEntry() {
		int l = ((Number) state.pop()).intValue();
		
		state.push(font.cvt.controlValues[l], 6);
	}
	
	public void execute() {
		int code = (int) table.getInstruction(state.instructionPointer);
		state.instructionPointer++;
		System.out.println("Code: " + Integer.toHexString(code));
		
		switch(code) {
		case 0x00:
		case 0x01:
			//SVTCA: Set Freedom & Prohection Vectors to Coordinate Axis
			setVectorsToAxis(code);
			break;
		case 0x02:
		case 0x03:
			//SPVTCA: Set Projection Vector to Coordinate Axis
			setProjectionVectorToAxis(code-0x02);
			break;
		case 0x04:
		case 0x05:
			//SFVTCA: Set Freedom Vector to Coordinate Axis
			setProjectionVectorToAxis(code-0x04);
			break;
		case 0x06:
		case 0x07:
			//SPVTL: Set Projection Vector to Line
			setProjectionVectorToLine(code-0x06);
			break;
		case 0x08:
		case 0x09:
			//SFVTL: Set Freedom Vector to Line
			setFreedomVectorToLine(code-0x08);
			break;
		case 0x0A:
			//SPVFS: Set Projection Vector From Stack
			setProjectionVectorFromStack();
			break;
		case 0x0B:
			//SFVFS: Set Freedom Vector From Stack
			setFreedomVectorFromStack();
			break;
		case 0x0C:
			//GPV: Get Projection Vector
			getProjectionVector();
			break;
		case 0x0D:
			//GFV: Get Freedom Vector
			getFreedomVector();
			break;
		case 0x0E:
			//SFVTPV: Set Freedom Vector to Projection Vector
			setFreedomVectorToProjectionVector();
			break;
		case 0x0F:
			//ISECT: Move Point P to the Intersection of Two Lines
			movePointToIntersection();
			break;
		case 0x10:
			//SRP0: Set Reference Point 0
			setReferencePoint(0);
			break;
		case 0x11:
			//SRP0: Set Reference Point 1
			setReferencePoint(1);
			break;
		case 0x12:
			//SRP0: Set Reference Point 2
			setReferencePoint(2);
			break;
		case 0x13:
			//SZP0: Set Zone Pointer 0
			setZonePointer(0);
			break;
		case 0x14:
			//SZP1: Set Zone Pointer 1
			setZonePointer(1);
			break;
		case 0x15:
			//SZP2: Set Zone Pointer 2
			setZonePointer(2);
			break;
		case 0x16:
			//SZPS: Set Zone Pointers
			setZonePointer(3);
			break;
		case 0x17:
			//SLOOP: Set Loop Variable
			state.loop = ((Number) state.pop()).intValue();
			break;
		case 0x18:
			//RTG: Round to Grid
			setRoundState(1);
			break;
		case 0x19:
			//RTHG: Rount to Half Grid
			setRoundState(2);
			break;
		case 0x1A:
			//SMD: Set Minimum Distance
			state.minDistance = ((Number) state.pop()).intValue();
			break;
		case 0x1B:
			//ELSE: Else Clause
			elseTest();
			break;
		case 0x1C:
			//JMPR: Jump Relative
			jmpr();
			break;
		case 0x1D:
			//SCVTCI: Set Control Value Table Cut-In
			setCVTCutIn();
			break;
		case 0x1E:
			//SSWCI: Set Single Width Cut-In
			state.singleWidthCutIn = ((Number) state.pop()).intValue();
			break;
		case 0x1F:
			//SSW: Set Single Width
			state.singleWidth = ((Number) state.pop()).intValue();
			break;
		case 0x20:
			//DUP: Duplicate the Top Stack Element
			state.stack.push(state.stack.peek());
			break;
		case 0x21:
			//POP: Pop Top Stack Element
			state.stack.pop();
			break;
		case 0x22:
			//CLEAR: Clear the Stack
			state.stack.clear();
			break;
		case 0x23:
			//SWAP: Swap the Top Two Elements on the Stack
			swap();
			break;
		case 0x24:
			//DEPTH: Depth of the Stack
			state.push(state.stack.size(), 5);
			break;
		case 0x25:
			//CINDEX: Copy the Indexed Element to the Top of the Stack
			copyIndex();
			break;
		case 0x26:
			//MINDEX: Move the Indexed Element to the Top of the Stack
			moveIndex();
			break;
		case 0x27:
			//ALIGNPTS: Align Points
			alignPoints();
			break;
		case 0x29:
			//UTP: Untouch Point
			untouchPoint();
			break;
		case 0x2A:
			//LOOPCALL: Loop and Call Function
			break;
		case 0x2B:
			//CALL: Call Function
			break;
		case 0x2C:
			//FDEF: Function Definition
			break;
		case 0x2D:
			//ENDF: End Function Definition
			break;
		case 0x2E:
		case 0x2F:
			//MDAP: Move Direct Absolute Point
			moveDirectAbsolutePoint(code-0x2F);
			break;
		case 0x30:
		case 0x31:
			//IUP: Interpolate Untouched Points Through the Outline
			interpolateUntouchedPoints(code-0x30);
			break;
		case 0x32:
		case 0x33:
			//SHP: Shift Point Using Reference Point
			shiftPoints(code-0x32);
			break;
		case 0x34:
		case 0x35:
			//SHC: Shift Contour Using Reference Point
			shiftContour(code-0x34);
			break;
		case 0x36:
		case 0x37:
			//SHZ: Shift Zone Using Reference Point
			shiftZone(code-0x36);
			break;
		case 0x38:
			//SHPIX: Shift Point by a Pixel Amount
			shiftPointByPixel();
			break;
		case 0x39:
			//IP: Interpolate Point
			interpolatePoint();
			break;
		case 0x3A:
		case 0x3B:
			//MSIRP: Move Stack Indirect Relative Point
			moveStackIndirectRelativePoint(code-0x3A);
			break;
		case 0x3C:
			//ALIGNRP: Align Reference Point
			alignReferencePoint();
			break;
		case 0x3D:
			//RTDG: Round to Double Grid
			setRoundState(3);
			break;
		case 0x3E:
		case 0x3F:
			//MIAP: Move Indirect Absolute Point
			moveIndirectAbsolutePoint(code - 0x3E);
			break;
		case 0x40:
			//NPUSHB: Push N Bytes
			pushNBytes();
			break;
		case 0x41:
			//NPUSHW: Push N Words
			pushNWords();
			break;
		case 0x42:
			//WS: Write Store
			writeStore();
			break;
		case 0x43:
			//RS: Read Store
			readStore();
			break;
		case 0x44:
			//WCVTP: Write Control Value Table in Pixel Units
			writeControlValueTablePixels();
			break;
		case 0x45:
			//RCVT: Read Control Value Table Entry
			readControlValueTableEntry();
			break;
		case 0x46: 
		case 0x47:
			//GC: Get Coordinate Projected Onto the Projection Vectior
			getCoordinate(code-0x46);
			break;
		case 0x48:
			//SCFS: Set Coordinate From the Stack Using Projection & Freedom Vectors
			setCoordinateFromStack();
			break;
		case 0x49:
		case 0x4A:
			//MD: Measure Distance
			measureDistance(code-0x49);
			break;
		case 0x4B:
			//MPPEM: Measure Pixels Per EM
			//This is not correct, but I couldn't find any better documentation.
			state.push(pixelsPerEm, 1);
			break;
		case 0x4C:
			//MPS: Measure Point Size
			state.push(pointSize, 1);
			break;
		case 0x4D:
			//FLIPON: Set the Auto FLIP Boolean to On
			state.autoFlip = true;
			break;
		case 0x4E:
			//FLIPOFF: Set the Auto FLIP Boolean to Off
			state.autoFlip = false;
			break;
		case 0x4F:
			//DEBUG: Debug
			//Pop integer so the system isn't broken.
			state.pop();
			break;
		case 0x50:
			//LT: Less Than
			lt();
			break;
		case 0x51:
			//LTEQ: Less Than or Equal
			lteq();
			break;
		case 0x52:
			//GT: Greater Than
			gt();
			break;
		case 0x53:
			//GTEQ: Greater Than or Equal
			gteq();
			break;
		case 0x54:
			//EQ: Equal
			eq();
			break;
		case 0x55:
			//NEQ: Not Equal
			neq();
			break;
		case 0x56:
			//ODD: Odd
			odd();
			break;
		case 0x57:
			//EVEN: Even
			even();
			break;
		case 0x58:
			//IF: If Test
			ifTest();
			break;
		case 0x59:
			//EIF: End IF
			//The isntruction is just a marker and doesn't do anything.
			break;
		case 0x5A:
			//AND: Logical And
			and();
			break;
		case 0x5B:
			//OR: Logical Or
			or();
			break;
		case 0x5C:
			//NOT: Logical Not
			not();
			break;
		case 0x5D:
			//DELTAP1: Delta Exception P1
			deltaP(1);
			break;
		case 0x5E:
			//SDB: Set Delta Base in the Graphics State
			state.deltaBase = ((Number) state.pop()).intValue();
			break;
		case 0x5F:
			//SDS: Set Delta Shift in the Graphics State
			state.deltaShift = ((Number) state.pop()).intValue();
			break;
		case 0x60:
			//ADD: Add
			add();
			break;
		case 0x61:
			//SUB: Subtract
			subtract();
			break;
		case 0x62:
			//DIV: Divide
			divide();
			break;
		case 0x63:
			//MUL: Multiply
			multiply();
			break;
		case 0x64:
			//ABS: Absolute Value
			absoluteValue();
			break;
		case 0x65:
			//NEG: Negate
			negate();
			break;
		case 0x66:
			//FLOOR: Floor
			floor();
			break;
		case 0x67:
			//CEILING: Ceiling
			ceiling();
			break;
		case 0x68:
		case 0x69:
		case 0x6A:
		case 0x6B:
			//ROUND: Round Value
			roundN(code-0x68);
			break;
		case 0x6C:
		case 0x6D:
		case 0x6E:
		case 0x6F:
			//NROUND: No Rounding of Value
			nRound(code-0x6C);
			break;
		case 0x70:
			//WCVTF: Write Control Value Table in Funits
			writeControlValueTableFunits();
			break;
		case 0x71:
			//DELTAP2: Delta Exception P2
			deltaP(2);
			break;
		case 0x72:
			//DELTAP3: Delta Exception P3
			deltaP(3);
			break;
		case 0x73:
			//DELTAC1: Delta Exception C1
			deltaC(1);
			break;
		case 0x74:
			//DELTAC2: Delta Exception C2
			deltaC(2);
			break;
		case 0x75:
			//DELTAC3: Delta Exception C3
			deltaC(3);
			break;
		case 0x76:
			//SROUND Super Round
			superRound();
			break;
		case 0x77:
			//S45ROUND: Super Round 45 Degrees
			set45Round();
			break;
		case 0x78:
			//JROT: Jump Relative on True
			jrot();
			break;
		case 0x79:
			//JROF: Jump Relative on False
			jrof();
			break;
		case 0x7A:
			//ROFF: Round Off
			setRoundState(0);
			break;
		case 0x7C:
			//RUTG: Round Up to Grid
			setRoundState(4);
			break;
		case 0x7D:
			//RDTG: Round Down to Grid
			setRoundState(5);
			break;
		case 0x7E:
			//SANGW: Set Angle Weight
			//There is very little description for this and I don't know what it does.
			state.pop();
			break;
		case 0x7F:
			//AA: Adjust Angle
			state.pop();
			break;
		case 0x80:
			//FLIPPT: Flip Point
			flipPoint();
			break;
		case 0x81:
			//FLIPRGON: Flip Range On
			flipRange(true);
			break;
		case 0x82:
			//FLIPRGOFF: Flip Range Off
			flipRange(false);
			break;
		case 0x85:
			//SCANCTRL: Scan Conversion Control
			break;
		case 0x86:
		case 0x87:
			//SDPVTL: Set Dual Projection Vector to Line
			setDualProjectionVectorToLine(code-0x86);
			break;
		case 0x88:
			//GETINFO: Get Information
			getInfo();
			break;
		case 0x89:
			//IDEF: Instruction Definition
			break;
		case 0x8A:
			//ROLL: Roll the Top Three Stack Elements
			roll();
			break;
		case 0x8B:
			//MAX: Maximum of Top Two Stack Elements
			maximum();
			break;
		case 0x8C:
			//MIN: Maximum of Top Two Stack Elements
			minimum();
			break;
		case 0x8D:
			//SCANTYPE: Scantype
			break;
		case 0x8E:
			//INSTCTRL: Instruction Execution Control
			break;
		case 0xB0:
		case 0xB1:
		case 0xB2:
		case 0xB3:
		case 0xB4:
		case 0xB5:
		case 0xB6:
		case 0xB7:
			//PUSHB: Push Bytes
			int pushbA = (code-0xB8)&1;
			int pushbB = ((code-0xB8)>>1)&1;
			int pushbC = ((code-0xB8)>>2)&1;
			pushBytes((4*pushbA) + (2*pushbB) + pushbC + 1);
			break;
		case 0xB8:
		case 0xB9:
		case 0xBA:
		case 0xBB:
		case 0xBC:
		case 0xBD:
		case 0xBE:
		case 0xBF:
			//PUSHW: Push Words
			int pushwA = (code-0xB8)&1;
			int pushwB = ((code-0xB8)>>1)&1;
			int pushwC = ((code-0xB8)>>2)&1;
			pushWords((4*pushwA) + (2*pushwB) + pushwC + 1);
			break;
		case 0xC0:
		case 0xC1:
		case 0xC2:
		case 0xC3:
		case 0xC4:
		case 0xC5:
		case 0xC6:
		case 0xC7:
		case 0xC8:
		case 0xC9:
		case 0xCA:
		case 0xCB:
		case 0xCC:
		case 0xCD:
		case 0xCE:
		case 0xCF:
		case 0xD0:
		case 0xD1:
		case 0xD2:
		case 0xD3:
		case 0xD4:
		case 0xD5:
		case 0xD6:
		case 0xD7:
		case 0xD8:
		case 0xD9:
		case 0xDA:
		case 0xDB:
		case 0xDC:
		case 0xDD:
		case 0xDE:
		case 0xDF:
			//MDRP: Move Direct Relative Point
			int mdrpa = (code-0xC0)|0x80;
			int mdrpb = (code-0xC0)|0x40;
			int mdrpc = (code-0xC0)|0x20;
			int mdrpde = (code-0xC0)|0x18;
			moveDirectRelativePoint(mdrpa, mdrpb, mdrpc, mdrpde);
			break;
		case 0xE0:
		case 0xE1:
		case 0xE2:
		case 0xE3:
		case 0xE4:
		case 0xE5:
		case 0xE6:
		case 0xE7:
		case 0xE8:
		case 0xE9:
		case 0xEA:
		case 0xEB:
		case 0xEC:
		case 0xED:
		case 0xEE:
		case 0xEF:
		case 0xF0:
		case 0xF1:
		case 0xF2:
		case 0xF3:
		case 0xF4:
		case 0xF5:
		case 0xF6:
		case 0xF7:
		case 0xF8:
		case 0xF9:
		case 0xFA:
		case 0xFB:
		case 0xFC:
		case 0xFD:
		case 0xFE:
		case 0xFF:
			//MIRP: Move Indirect Relative Point
			int mirpa = (code-0xC0)|0x80;
			int mirpb = (code-0xC0)|0x40;
			int mirpc = (code-0xC0)|0x20;
			int mirpde = (code-0xC0)|0x18;
			moveIndirectRelativePoint(mirpa, mirpb, mirpc, mirpde);
			break;
		}
	}
}