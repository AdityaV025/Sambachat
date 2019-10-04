//@Author - Aditya_Verma
#include <iostream>6

using namespace std;

void EgyptianFrac(int nem, int dem) {

    //runs if the numerator or denominator is zero.
    if(nem == 0 || dem == 0) {

        cout<<"Please insert a valid integer.";
        return;

    }

    //If the numerator divides the denominator, hence the simple fraction will be printed.
    else if(nem%dem == 0) {

         cout<<nem/dem;
         return;

    }

    //runs if the denominator divides the numerator perfectly, hence the inserted number is not a fraction.
    else if(dem%nem == 0) {

        cout<<"1/"<<dem/nem;
        return;

    }

    //runs if the numerator is greater than the denominator.
    else if(nem>dem) {

        cout<<nem/dem << " + ";
        EgyptianFrac(nem%dem, dem);
        return;

    }

    int n = dem/nem + 1;
    cout<<"1/"<<n<<" + ";

    EgyptianFrac(nem*n-dem, dem*n);

}

int main() {

    int nem,dem;

    cout<<"Enter Numerator: ";
    cin>>nem;

    cout<<"Enter Denominator: ";
    cin>>dem;

    cout << "The Representation in Egyptian Fraction of Fraction: "
         << nem << "/" << dem << " is\n ";
    EgyptianFrac(nem, dem);

    return 0;

}
