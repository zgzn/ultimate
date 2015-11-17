package de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.valuedomain.octagon;

import java.util.Arrays;
import java.util.function.BiFunction;

// Stores entries for variables +v1, -v1, ..., +vn, -vn
// Matrix is always coherent
// Elements on the main diagonal are not stored and have value 0 at all times
public class OctMatrix {

	// Only some parts of the matrix are stored: upper (U) and lower (L).
	//
	// \ U U U U U   <= OctagonMatrix for 3 variables
	// L \ U U U U      Each variable comes in two flavors: + and -
	// . . \ U U U      Matrix is divided into 2x2 blocks
	// . . L \ U U      Variables (names, type, ...) aren't stored in OctMatrix
	// . . . . \ U
	// . . . . L \

	// upper triangular part of the matrix, without the main diagonal
	// stored row-wise from top to bottom, left to right
	private OctValue[] mUpper;
	
	// part below the main diagonal
	private OctValue[] mLower;
	
	private int mSize;

	public OctMatrix(OctMatrix octMatrix) {
		mSize = octMatrix.mSize;
		mUpper = new OctValue[octMatrix.mUpper.length];
		mLower = new OctValue[octMatrix.mLower.length];
		Arrays.fill(mUpper, OctValue.INFINITY);
		Arrays.fill(mLower, OctValue.INFINITY);
	}
	
	public OctMatrix(int size) {
		mSize = size;
		mUpper = new OctValue[size * (size - 1) / 2];
		mLower = new OctValue[size / 2];
		Arrays.fill(mUpper, OctValue.INFINITY);
		Arrays.fill(mLower, OctValue.INFINITY);
	}

	public OctValue get(int row, int col) {
		if (row < col)
			return mUpper[upperIndex(row, col)];
		if (row == col)
			return OctValue.ZERO;
		if ((row ^ col) == 1) // && row > col
			return mLower[col / 2];
		return mUpper[upperIndex(col ^ 1, row ^ 1)];
	}
	
	protected void set(int row, int col, OctValue value) {
		assert value != null : "null is not a valid matrix element.";
		if (row < col) {
			mUpper[upperIndex(row, col)] = value;
		} else if (row == col) {
			throw new UnsupportedOperationException("Elements on main diagonal are constant and cannot be set.");
		} else if ((row ^ col) == 1) { // && row > col
			mLower[col / 2] = value;
		} else {
			mUpper[upperIndex(col ^ 1, row ^ 1)] = value;
		}
	}

	private int upperIndex(int row, int col) {
		int rectIndex = row * mSize + col; // index if the complete rectangular matrix was stored row-wise
		int skipped = (row + 1) * (row + 2) / 2; // skipped fields from the complete matrix
		return rectIndex - skipped;
	}
	
	public OctMatrix elementwise(OctMatrix other, BiFunction<OctValue, OctValue, OctValue> operator) {
		assert other.mSize == mSize : "Incompatible matrices";
		OctMatrix result = new OctMatrix(mSize);
		// loops are faster than streams
		for (int i = 0; i < mUpper.length; ++i)
			result.mUpper[i] = operator.apply(mUpper[i], other.mUpper[i]);
		for (int i = 0; i < mLower.length; ++i)
			result.mUpper[i] = operator.apply(mUpper[i], other.mUpper[i]);
		return result;
	}

	public OctMatrix add(OctMatrix other) {
		return elementwise(other, (x, y) -> x.add(y));
	}
	
	public OctMatrix min(OctMatrix other) {
		return elementwise(other, OctValue::min);
	}
	
	public OctMatrix max(OctMatrix other) {
		return elementwise(other, OctValue::max);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int row = 0; row < mSize; ++row) {
			String delimiter = "";
			for (int col = 0; col < mSize; ++col) {
				sb.append(delimiter);
				sb.append(get(row, col));
				delimiter = "\t";
			}
			sb.append("\n");
		}
		return sb.toString();
	}
}
