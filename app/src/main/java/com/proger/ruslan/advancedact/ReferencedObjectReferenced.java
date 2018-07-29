package com.proger.ruslan.advancedact;

public class ReferencedObjectReferenced {
    private double externalSize, actualSizeVector, koeff;

    public ReferencedObjectReferenced(String referencedObject, double actualSizeVector) {
        double externalSize = 0;

        switch (referencedObject) {
            case "debitCard":
                externalSize = 85.6d;
                break;
            case "pen":
                externalSize = 130d;
                break;
            case "book":
                externalSize = 200d;
                break;
            case "avg":
                externalSize = 1;
                break;
        }

        this.externalSize = externalSize;
        this.actualSizeVector = actualSizeVector;

        setKoeff();
    }

    private void setKoeff () {
        try {
            koeff = externalSize / actualSizeVector;
        } catch (ArithmeticException e) {
            koeff = 1;
        }
    }

    public void setActualSizeVector (double actualSizeVector) {
        try {
            this.actualSizeVector = actualSizeVector;

            setKoeff();
        } catch (ArithmeticException e) {
            this.actualSizeVector = 1;
        }
    }

    public double getKoeff () {
        return koeff;
    }

    public double getSize (double relative) {
        return relative*koeff;
    }
}
