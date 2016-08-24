package demo;

import lib.SafeParcelable;

/**
 * Created by tom on 24.08.16.
 */
public class AnotherModel
        implements IModel
{

    @SafeParcelable(1)
    int intField;

    @SafeParcelable(2)
    String stringField;

    @SafeParcelable(3)
    boolean booleanField;

    //#writeToParcel
}