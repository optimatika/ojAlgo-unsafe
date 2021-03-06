/*
 * Copyright 1997-2021 Optimatika
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.ojalgo.array;

import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.FunctionSet;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.VoidFunction;
import org.ojalgo.function.aggregator.AggregatorSet;
import org.ojalgo.function.aggregator.PrimitiveAggregator;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.scalar.PrimitiveScalar;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.Access1D;

/**
 * <p>
 * Off heap memory array. Currently supports float and double elements
 * </p>
 *
 * @author apete
 */
public abstract class OffHeapArray extends DenseArray<Double> {

    public static final Factory<Double> NATIVE32 = new Factory<Double>() {

        @Override
        public AggregatorSet<Double> aggregator() {
            return PrimitiveAggregator.getSet();
        }

        @Override
        public FunctionSet<Double> function() {
            return PrimitiveFunction.getSet();
        }

        @Override
        public Scalar.Factory<Double> scalar() {
            return PrimitiveScalar.FACTORY;
        }

        @Override
        long getCapacityLimit() {
            return MAX_ARRAY_SIZE;
        }

        @Override
        long getElementSize() {
            return Native32Array.ELEMENT_SIZE;
        }

        @Override
        public DenseArray<Double> makeDenseArray(final long size) {
            return new Native32Array(size);
        }

    };

    public static final Factory<Double> NATIVE64 = new Factory<Double>() {

        @Override
        public AggregatorSet<Double> aggregator() {
            return PrimitiveAggregator.getSet();
        }

        @Override
        public FunctionSet<Double> function() {
            return PrimitiveFunction.getSet();
        }

        @Override
        public Scalar.Factory<Double> scalar() {
            return PrimitiveScalar.FACTORY;
        }

        @Override
        long getCapacityLimit() {
            return MAX_ARRAY_SIZE;
        }

        @Override
        long getElementSize() {
            return Native64Array.ELEMENT_SIZE;
        }

        @Override
        public DenseArray<Double> makeDenseArray(final long size) {
            return new Native64Array(size);
        }

    };

    public static OffHeapArray makeNative32(final long count) {
        return new Native32Array(count);
    }

    public static OffHeapArray makeNative64(final long count) {
        return new Native64Array(count);
    }

    private final long myCount;

    OffHeapArray(final DenseArray.Factory<Double> factory, final long count) {

        super(factory);

        myCount = count;
    }

    public void add(final long index, final double addend) {
        this.set(index, this.doubleValue(index) + addend);
    }

    public void add(final long index, final Comparable<?> addend) {
        this.add(index, Scalar.doubleValue(addend));
    }

    public long count() {
        return myCount;
    }

    public void fillAll(final Double value) {
        this.fill(0L, this.count(), 1L, value);
    }

    public void fillOne(final long index, final Access1D<?> values, final long valueIndex) {
        this.set(index, values.doubleValue(valueIndex));
    }

    public void fillOne(final long index, final Double value) {
        this.set(index, value.doubleValue());
    }

    public void fillOne(final long index, final NullaryFunction<?> supplier) {
        this.set(index, supplier.doubleValue());
    }

    public Double get(final long index) {
        return this.doubleValue(index);
    }

    public boolean isAbsolute(final long index) {
        return PrimitiveScalar.isAbsolute(this.doubleValue(index));
    }

    public boolean isSmall(final long index, final double comparedTo) {
        return PrimitiveScalar.isSmall(this.doubleValue(index), comparedTo);
    }

    public void modifyOne(final long index, final UnaryFunction<Double> modifier) {
        this.set(index, modifier.invoke(this.doubleValue(index)));
    }

    @Override
    public final void reset() {
        this.fillAll(PrimitiveMath.ZERO);
    }

    public void set(final long index, final Comparable<?> value) {
        this.set(index, Scalar.doubleValue(value));
    }

    public void visitOne(final long index, final VoidFunction<Double> visitor) {
        visitor.accept(this.doubleValue(index));
    }

    @Override
    protected void exchange(final long firstA, final long firstB, final long step, final long count) {

        long tmpIndexA = firstA;
        long tmpIndexB = firstB;

        double tmpVal;

        for (long i = 0; i < count; i++) {

            tmpVal = this.doubleValue(tmpIndexA);
            this.set(tmpIndexA, this.doubleValue(tmpIndexB));
            this.set(tmpIndexB, tmpVal);

            tmpIndexA += step;
            tmpIndexB += step;
        }
    }

    @Override
    protected void fill(final long first, final long limit, final long step, final Double value) {
        final double tmpValue = value.doubleValue();
        for (long i = first; i < limit; i += step) {
            this.set(i, tmpValue);
        }
    }

    @Override
    protected void fill(final long first, final long limit, final long step, final NullaryFunction<?> supplier) {
        for (long i = first; i < limit; i += step) {
            this.set(i, supplier.doubleValue());
        }
    }

    @Override
    protected boolean isSmall(final long first, final long limit, final long step, final double comparedTo) {

        boolean retVal = true;

        for (long i = first; retVal && (i < limit); i += step) {
            retVal &= PrimitiveScalar.isSmall(comparedTo, this.doubleValue(i));
        }

        return retVal;
    }

    @Override
    protected void modify(final long first, final long limit, final long step, final Access1D<Double> left, final BinaryFunction<Double> function) {
        for (long i = first; i < limit; i += step) {
            this.set(i, function.invoke(left.doubleValue(i), this.doubleValue(i)));
        }
    }

    @Override
    protected void modify(final long first, final long limit, final long step, final BinaryFunction<Double> function, final Access1D<Double> right) {
        for (long i = first; i < limit; i += step) {
            this.set(i, function.invoke(this.doubleValue(i), right.doubleValue(i)));
        }
    }

    @Override
    protected void modify(final long first, final long limit, final long step, final UnaryFunction<Double> function) {
        for (long i = first; i < limit; i += step) {
            this.set(i, function.invoke(this.doubleValue(i)));
        }
    }

    @Override
    protected void visit(final long first, final long limit, final long step, final VoidFunction<Double> visitor) {
        for (long i = first; i < limit; i += step) {
            visitor.invoke(this.doubleValue(i));
        }
    }

    @Override
    boolean isPrimitive() {
        return true;
    }

    @Override
    void modify(final long extIndex, final int intIndex, final Access1D<Double> left, final BinaryFunction<Double> function) {
        this.set(intIndex, function.invoke(left.doubleValue(extIndex), this.doubleValue(intIndex)));
    }

    @Override
    void modify(final long extIndex, final int intIndex, final BinaryFunction<Double> function, final Access1D<Double> right) {
        this.set(intIndex, function.invoke(this.doubleValue(intIndex), right.doubleValue(extIndex)));
    }

    @Override
    void modify(final long extIndex, final int intIndex, final UnaryFunction<Double> function) {
        this.set(intIndex, function.invoke(this.doubleValue(intIndex)));
    }

}
