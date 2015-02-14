package tim.prune.data;

/**
 * Class to represent a lat/long coordinate
 * and provide conversion functions
 */
public abstract class Coordinate
{
	public static final int NO_CARDINAL = -1;
	public static final int NORTH = 0;
	public static final int EAST = 1;
	public static final int SOUTH = 2;
	public static final int WEST = 3;
	public static final char[] PRINTABLE_CARDINALS = {'N', 'E', 'S', 'W'};
	public static final int FORMAT_DEG_MIN_SEC = 10;
	public static final int FORMAT_DEG_MIN = 11;
	public static final int FORMAT_DEG = 12;
	public static final int FORMAT_DEG_WITHOUT_CARDINAL = 13;
	public static final int FORMAT_DEG_WHOLE_MIN = 14;
	public static final int FORMAT_DEG_MIN_SEC_WITH_SPACES = 15;
	public static final int FORMAT_CARDINAL = 16;
	public static final int FORMAT_NONE = 19;

	// Instance variables
	private boolean _valid = false;
	protected int _cardinal = NORTH;
	private int _degrees = 0;
	private int _minutes = 0;
	private int _seconds = 0;
	private int _fracs = 0;
	private int _fracDenom = 0;
	private String _originalString = null;
	private int _originalFormat = FORMAT_NONE;
	private double _asDouble = 0.0;


	/**
	 * Constructor given String
	 * @param inString string to parse
	 */
	public Coordinate(String inString)
	{
		_originalString = inString;
		int strLen = 0;
		if (inString != null)
		{
			inString = inString.trim();
			strLen = inString.length();
		}
		if (strLen > 1)
		{
			// Check for cardinal character either at beginning or end
			_cardinal = getCardinal(inString.charAt(0), inString.charAt(strLen-1));
			// count numeric fields - 1=d, 2=dm, 3=dm.m/dms, 4=dms.s
			int numFields = 0;
			boolean inNumeric = false;
			char currChar;
			long[] fields = new long[4]; // needs to be long for lengthy decimals
			long[] denoms = new long[4];
			String secondDelim = "";
			try
			{
				// Loop over characters in input string, populating fields array
				for (int i=0; i<strLen; i++)
				{
					currChar = inString.charAt(i);
					if (currChar >= '0' && currChar <= '9')
					{
						if (!inNumeric)
						{
							inNumeric = true;
							numFields++;
							denoms[numFields-1] = 1;
						}
						fields[numFields-1] = fields[numFields-1] * 10 + (currChar - '0');
						denoms[numFields-1] *= 10;
					}
					else
					{
						inNumeric = false;
						// Remember second delimiter
						if (numFields == 2) {
							secondDelim += currChar;
						}
					}
				}
				_valid = (numFields > 0);
			}
			catch (ArrayIndexOutOfBoundsException obe)
			{
				// more than four fields found - unable to parse
				_valid = false;
			}
			// parse fields according to number found
			_degrees = (int) fields[0];
			_originalFormat = FORMAT_DEG;
			_fracDenom = 10;
			if (numFields == 2)
			{
				// String is just decimal degrees
				double numMins = fields[1] * 60.0 / denoms[1];
				_minutes = (int) numMins;
				double numSecs = (numMins - _minutes) * 60.0;
				_seconds = (int) numSecs;
				_fracs = (int) ((numSecs - _seconds) * 10);
			}
			// Differentiate between d-m.f and d-m-s using . or ,
			else if (numFields == 3 && (secondDelim.equals(".") || secondDelim.equals(",")))
			{
				// String is degrees-minutes.fractions
				_originalFormat = FORMAT_DEG_MIN;
				_minutes = (int) fields[1];
				double numSecs = fields[2] * 60.0 / denoms[2];
				_seconds = (int) numSecs;
				_fracs = (int) ((numSecs - _seconds) * 10);
			}
			else if (numFields == 4 || numFields == 3)
			{
				// String is degrees-minutes-seconds.fractions
				_originalFormat = FORMAT_DEG_MIN_SEC;
				_minutes = (int) fields[1];
				_seconds = (int) fields[2];
				_fracs = (int) fields[3];
				_fracDenom = (int) denoms[3];
				if (_fracDenom < 1) {_fracDenom = 1;}
			}
			_asDouble = 1.0 * _degrees + (_minutes / 60.0) + (_seconds / 3600.0) + (_fracs / 3600.0 / _fracDenom);
			if (_cardinal == WEST || _cardinal == SOUTH || inString.charAt(0) == '-')
				_asDouble = -_asDouble;
			// validate fields
			_valid = _valid && (_degrees <= getMaxDegrees() && _minutes < 60 && _seconds < 60 && _fracs < _fracDenom);
		}
		else _valid = false;
	}


	/**
	 * Get the cardinal from the given character
	 * @param inFirstChar first character from file
	 * @param inLastChar last character from file
	 */
	protected int getCardinal(char inFirstChar, char inLastChar)
	{
		// Try leading character first
		int cardinal = getCardinal(inFirstChar);
		// if not there, try trailing character
		if (cardinal == NO_CARDINAL) {
			cardinal = getCardinal(inLastChar);
		}
		// use default from concrete subclass
		if (cardinal == NO_CARDINAL) {
			cardinal = getDefaultCardinal();
		}
		return cardinal;
	}


	/**
	 * Get the cardinal from the given character
	 * @param inChar character from file
	 */
	protected abstract int getCardinal(char inChar);

	/**
	 * @return the default cardinal for the subclass
	 */
	protected abstract int getDefaultCardinal();

	/**
	 * @return the maximum degree range for this coordinate
	 */
	protected abstract int getMaxDegrees();


	/**
	 * Constructor
	 * @param inValue value of coordinate
	 * @param inFormat format to use
	 * @param inCardinal cardinal
	 */
	protected Coordinate(double inValue, int inFormat, int inCardinal)
	{
		_asDouble = inValue;
		// Calculate degrees, minutes, seconds
		_degrees = (int) Math.abs(inValue);
		double numMins = (Math.abs(_asDouble)-_degrees) * 60.0;
		_minutes = (int) numMins;
		double numSecs = (numMins - _minutes) * 60.0;
		_seconds = (int) numSecs;
		_fracs = (int) ((numSecs - _seconds) * 10);
		_fracDenom = 10; // fixed for now
		// Make a string to display on screen
		_cardinal = inCardinal;
		_originalFormat = FORMAT_NONE;
		if (inFormat == FORMAT_NONE) inFormat = FORMAT_DEG_WITHOUT_CARDINAL;
		_originalString = output(inFormat);
		_originalFormat = inFormat;
		_valid = true;
	}


	/**
	 * @return coordinate as a double
	 */
	public double getDouble()
	{
		return _asDouble;
	}

	/**
	 * @return true if Coordinate is valid
	 */
	public boolean isValid()
	{
		return _valid;
	}

	/**
	 * Compares two Coordinates for equality
	 * @param inOther other Coordinate object with which to compare
	 * @return true if the two objects are equal
	 */
	public boolean equals(Coordinate inOther)
	{
		return (inOther != null && _cardinal == inOther._cardinal
			&& _degrees == inOther._degrees
			&& _minutes == inOther._minutes
			&& _seconds == inOther._seconds
			&& _fracs == inOther._fracs);
	}


	/**
	 * Output the Coordinate in the given format
	 * @param inFormat format to use, eg FORMAT_DEG_MIN_SEC
	 * @return String for output
	 */
	public String output(int inFormat)
	{
		String answer = _originalString;
		if (inFormat != FORMAT_NONE && inFormat != _originalFormat)
		{
			// TODO: allow specification of precision for output of d-m and d
			// format as specified
			switch (inFormat)
			{
				case FORMAT_DEG_MIN_SEC:
				{
					StringBuffer buffer = new StringBuffer();
					buffer.append(PRINTABLE_CARDINALS[_cardinal])
						.append(threeDigitString(_degrees)).append('°')
						.append(twoDigitString(_minutes)).append('\'')
						.append(twoDigitString(_seconds)).append('.')
						.append(formatFraction(_fracs, _fracDenom));
					answer = buffer.toString();
					break;
				}
				case FORMAT_DEG_MIN:
				{
					answer = "" + PRINTABLE_CARDINALS[_cardinal] + threeDigitString(_degrees) + "°"
						+ (_minutes + _seconds / 60.0 + _fracs / 60.0 / _fracDenom) + "'";
					break;
				}
				case FORMAT_DEG_WHOLE_MIN:
				{
					answer = "" + PRINTABLE_CARDINALS[_cardinal] + threeDigitString(_degrees) + "°"
						+ (int) Math.floor(_minutes + _seconds / 60.0 + _fracs / 60.0 / _fracDenom + 0.5) + "'";
					break;
				}
				case FORMAT_DEG:
				case FORMAT_DEG_WITHOUT_CARDINAL:
				{
					answer = (_asDouble<0.0?"-":"")
						+ (_degrees + _minutes / 60.0 + _seconds / 3600.0 + _fracs / 3600.0 / _fracDenom);
					break;
				}
				case FORMAT_DEG_MIN_SEC_WITH_SPACES:
				{
					// Note: cardinal not needed as this format is only for exif, which has cardinal separately
					answer = "" + _degrees + " " + _minutes + " " + _seconds + "." + formatFraction(_fracs, _fracDenom);
					break;
				}
				case FORMAT_CARDINAL:
				{
					answer = "" + PRINTABLE_CARDINALS[_cardinal];
					break;
				}
			}
		}
		return answer;
	}

	/**
	 * Format the fraction part of seconds value
	 * @param inFrac fractional part eg 123
	 * @param inDenom denominator of fraction eg 10000
	 * @return String describing fraction, in this case 0123
	 */
	private static final String formatFraction(int inFrac, int inDenom)
	{
		if (inDenom <= 1 || inFrac == 0) {return "" + inFrac;}
		String denomString = "" + inDenom;
		int reqdLen = denomString.length() - 1;
		String result = denomString + inFrac;
		int resultLen = result.length();
		return result.substring(resultLen - reqdLen);
	}


	/**
	 * Format an integer to a two-digit String
	 * @param inNumber number to format
	 * @return two-character String
	 */
	private static String twoDigitString(int inNumber)
	{
		if (inNumber <= 0) return "00";
		if (inNumber < 10) return "0" + inNumber;
		if (inNumber < 100) return "" + inNumber;
		return "" + (inNumber % 100);
	}


	/**
	 * Format an integer to a three-digit String for degrees
	 * @param inNumber number to format
	 * @return three-character String
	 */
	private static String threeDigitString(int inNumber)
	{
		if (inNumber <= 0) return "000";
		if (inNumber < 10) return "00" + inNumber;
		if (inNumber < 100) return "0" + inNumber;
		return "" + (inNumber % 1000);
	}


	/**
	 * Create a new Coordinate between two others
	 * @param inStart start coordinate
	 * @param inEnd end coordinate
	 * @param inIndex index of point
	 * @param inNumPoints number of points to interpolate
	 * @return new Coordinate object
	 */
	public static Coordinate interpolate(Coordinate inStart, Coordinate inEnd,
		int inIndex, int inNumPoints)
	{
		return interpolate(inStart, inEnd, 1.0 * (inIndex+1) / (inNumPoints + 1));
	}


	/**
	 * Create a new Coordinate between two others
	 * @param inStart start coordinate
	 * @param inEnd end coordinate
	 * @param inFraction fraction from start to end
	 * @return new Coordinate object
	 */
	public static Coordinate interpolate(Coordinate inStart, Coordinate inEnd,
		double inFraction)
	{
		double startValue = inStart.getDouble();
		double endValue = inEnd.getDouble();
		double newValue = startValue + (endValue - startValue) * inFraction;
		Coordinate answer = inStart.makeNew(newValue, inStart._originalFormat);
		return answer;
	}


	/**
	 * Make a new Coordinate according to subclass
	 * @param inValue double value
	 * @param inFormat format to use
	 * @return object of Coordinate subclass
	 */
	protected abstract Coordinate makeNew(double inValue, int inFormat);


	/**
	 * Create a String representation for debug
	 * @return String describing coordinate value
	 */
	public String toString()
	{
		return "Coord: " + _cardinal + " (" + _degrees + ") (" + _minutes + ") (" + _seconds + "."
			+ formatFraction(_fracs, _fracDenom) + ") = " + _asDouble;
	}
}
