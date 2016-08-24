package demo;

import lib.SafeParcelable;

/**
 * Created by tom on 24.08.16.
 */
public class Model implements IModel {

    @SafeParcelable(1)
    int intField;

    @SafeParcelable(2)
    String stringField;

    @Override
    public void writeToParcel() {

    }
}
 //new method