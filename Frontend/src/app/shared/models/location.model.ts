export interface DistrictConfig {
  name: string;
  cities: string[];
}

export interface StateConfig {
  name: string;
  districts: DistrictConfig[];
}

export interface CountryConfig {
  name: string;
  states: StateConfig[];
}

export const LOCATION_DATA: CountryConfig[] = [
  {
    name: 'India',
    states: [
      {
        name: 'Tamil Nadu',
        districts: [
          { name: 'Chennai', cities: ['Chennai City', 'Ambattur', 'Tambaram', 'Avadi', 'Velachery', 'Guindy'] },
          { name: 'Coimbatore', cities: ['Coimbatore City', 'Pollachi', 'Mettupalayam', 'Valparai', 'Sulur'] },
          { name: 'Madurai', cities: ['Madurai City', 'Tirumangalam', 'Melur', 'Vadipatti'] },
          { name: 'Tiruchirappalli', cities: ['Trichy City', 'Srirangam', 'Thuvakudi', 'Lalgudi', 'Manapparai'] },
          { name: 'Villupuram', cities: ['Villupuram City', 'Tindivanam', 'Gingee', 'Vikravandi', 'Valavanur', 'Kottakuppam', 'Marakkanam'] },
          { name: 'Salem', cities: ['Salem City', 'Attur', 'Mettur', 'Omalur', 'Edappadi'] },
          { name: 'Tirunelveli', cities: ['Tirunelveli City', 'Ambasamudram', 'Nanguneri', 'Valliyur'] },
          { name: 'Kanchipuram', cities: ['Kanchipuram City', 'Sriperumbudur', 'Uttiramerur', 'Walajabad'] },
          { name: 'Chengalpattu', cities: ['Chengalpattu City', 'Tambaram South', 'Mahabalipuram', 'Guduvancheri', 'Maraimalai Nagar'] },
          { name: 'Vellore', cities: ['Vellore City', 'Katpadi', 'Gudiyattam', 'Anaicut'] },
          { name: 'Erode', cities: ['Erode City', 'Gobichettipalayam', 'Bhavani', 'Perundurai'] },
          { name: 'Thanjavur', cities: ['Thanjavur City', 'Kumbakonam', 'Pattukkottai', 'Orathanadu'] },
          { name: 'Kanyakumari', cities: ['Nagercoil', 'Padmanabhapuram', 'Kuzhithurai', 'Colachel'] }
        ]
      },
      {
        name: 'Maharashtra',
        districts: [
          { name: 'Mumbai Suburban', cities: ['Andheri', 'Bandra', 'Borivali', 'Kurla', 'Goregaon', 'Malad', 'Ghatkopar'] },
          { name: 'Mumbai City', cities: ['Colaba', 'Dadarm', 'Nariman Point', 'Worli', 'Byculla'] },
          { name: 'Pune', cities: ['Pune City', 'Pimpri-Chinchwad', 'Baramati', 'Hinjawadi', 'Chakan', 'Lonavala'] },
          { name: 'Nagpur', cities: ['Nagpur City', 'Kamptee', 'Umred', 'Hingna', 'Katol'] },
          { name: 'Thane', cities: ['Thane City', 'Kalyan', 'Dombivli', 'Navi Mumbai', 'Bhiwandi', 'Mira-Bhayandar'] },
          { name: 'Nashik', cities: ['Nashik City', 'Malegaon', 'Sinnar', 'Igatpuri', 'Manmad'] },
          { name: 'Chhatrapati Sambhajinagar', cities: ['Chhatrapati Sambhajinagar City', 'Paithan', 'Sillod', 'Gangapur'] },
          { name: 'Solapur', cities: ['Solapur City', 'Pandharpur', 'Barshi', 'Akkalkot'] },
          { name: 'Kolhapur', cities: ['Kolhapur City', 'Ichalkaranji', 'Kagal', 'Jaysingpur'] }
        ]
      },
      {
        name: 'Karnataka',
        districts: [
          { name: 'Bengaluru Urban', cities: ['Bengaluru City', 'Yelahanka', 'Whitefield', 'Electronic City', 'Kengeri', 'Jayanagar'] },
          { name: 'Mysuru', cities: ['Mysuru City', 'Hunsur', 'Nanjangud', 'T. Narsipur', 'K.R. Nagar'] },
          { name: 'Dharwad', cities: ['Hubballi', 'Dharwad City', 'Navalgund', 'Kalghatgi'] },
          { name: 'Dakshina Kannada', cities: ['Mangaluru', 'Ullal', 'Puttur', 'Bantwal', 'Belthangady'] },
          { name: 'Belagavi', cities: ['Belagavi City', 'Gokak', 'Chikkodi', 'Bailhongal'] },
          { name: 'Kalaburagi', cities: ['Kalaburagi City', 'Sedam', 'Aland', 'Shahabad'] },
          { name: 'Shivamogga', cities: ['Shivamogga City', 'Bhadravathi', 'Sagara', 'Shikaripura'] },
          { name: 'Udupi', cities: ['Udupi City', 'Kundapura', 'Karkala', 'Kaup'] }
        ]
      },
      {
        name: 'Telangana',
        districts: [
          { name: 'Hyderabad', cities: ['Hyderabad City', 'Secunderabad', 'Khairatabad', 'Ameerpet', 'Banjara Hills', 'Jubilee Hills'] },
          { name: 'Medchal-Malkajgiri', cities: ['Kukatpally', 'Malkajgiri', 'Medchal', 'Quthbullapur', 'Uppal'] },
          { name: 'Warangal', cities: ['Warangal City', 'Kazipet', 'Hanamkonda', 'Narsampet'] },
          { name: 'Rangareddy', cities: ['Gachibowli', 'Cyberabad', 'Rajendranagar', 'Ibrahimpatnam', 'Shadnagar'] },
          { name: 'Karimnagar', cities: ['Karimnagar City', 'Huzurabad', 'Jagtial', 'Choppadandi'] },
          { name: 'Nizamabad', cities: ['Nizamabad City', 'Bodhan', 'Armoor'] }
        ]
      },
      {
        name: 'Andhra Pradesh',
        districts: [
          { name: 'Visakhapatnam', cities: ['Visakhapatnam City', 'Gajuwaka', 'Anakapalle', 'Bheemunipatnam'] },
          { name: 'NTR', cities: ['Vijayawada', 'Ibrahimpatnam', 'Jaggayyapeta', 'Nandigama'] },
          { name: 'Guntur', cities: ['Guntur City', 'Tenali', 'Mangalagiri', 'Ponnur'] },
          { name: 'Tirupati', cities: ['Tirupati City', 'Srikalahasti', 'Gudur', 'Sullurpeta'] },
          { name: 'Kurnool', cities: ['Kurnool City', 'Adoni', 'Yemmiganur', 'Dhone'] },
          { name: 'Kakinada', cities: ['Kakinada City', 'Pithapuram', 'Samalkota', 'Tuni'] }
        ]
      },
      {
        name: 'Delhi (UT)',
        districts: [
          { name: 'New Delhi', cities: ['Connaught Place', 'Chanakyapuri', 'Vasant Vihar', 'Barakhamba'] },
          { name: 'South Delhi', cities: ['Saket', 'Hauz Khas', 'Mehrauli', 'Greater Kailash'] },
          { name: 'East Delhi', cities: ['Preet Vihar', 'Mayur Vihar', 'Shahdara', 'Laxmi Nagar'] },
          { name: 'North Delhi', cities: ['Civil Lines', 'Sadar Bazar', 'Model Town', 'Kotwali'] },
          { name: 'West Delhi', cities: ['Rajouri Garden', 'Janakpuri', 'Punjabi Bagh', 'Dwarka'] },
          { name: 'Central Delhi', cities: ['Daryaganj', 'Paharganj', 'Karol Bagh'] }
        ]
      },
      {
        name: 'Gujarat',
        districts: [
          { name: 'Ahmedabad', cities: ['Ahmedabad City', 'Bodakdev', 'Satellite', 'Sanand', 'Maninagar', 'Navrangpura'] },
          { name: 'Surat', cities: ['Surat City', 'Rander', 'Adajan', 'Varachha', 'Udhna'] },
          { name: 'Vadodara', cities: ['Vadodara City', 'Alkapuri', 'Manjalpur', 'Makarpura', 'Padra'] },
          { name: 'Rajkot', cities: ['Rajkot City', 'Morbi', 'Gondal', 'Jetpur'] },
          { name: 'Bhavnagar', cities: ['Bhavnagar City', 'Palitana', 'Mahuva', 'Shihor'] },
          { name: 'Jamnagar', cities: ['Jamnagar City', 'Dwarka', 'Khambhalia'] }
        ]
      },
      {
        name: 'West Bengal',
        districts: [
          { name: 'Kolkata', cities: ['Kolkata City', 'Alipore', 'Ballygunge', 'Tollygunge', 'Salt Lake (Sector 1)', 'Park Street'] },
          { name: 'North 24 Parganas', cities: ['Bidhannagar / Salt Lake', 'Rajarhat', 'New Town', 'Dum Dum', 'Barasat', 'Barrackpore'] },
          { name: 'Darjeeling', cities: ['Darjeeling City', 'Siliguri', 'Kurseong', 'Mirik'] },
          { name: 'Howrah', cities: ['Howrah City', 'Bally', 'Uluberia', 'Shibpur'] },
          { name: 'Hooghly', cities: ['Chinsurah', 'Chandannagar', 'Serampore', 'Rishra', 'Arambagh'] },
          { name: 'Paschim Bardhaman', cities: ['Asansol', 'Durgapur', 'Raniganj'] }
        ]
      },
      {
        name: 'Uttar Pradesh',
        districts: [
          { name: 'Lucknow', cities: ['Lucknow City', 'Gomti Nagar', 'Hazratganj', 'Alambagh', 'Indira Nagar'] },
          { name: 'Gautam Buddha Nagar', cities: ['Noida', 'Greater Noida', 'Dadri', 'Jewar'] },
          { name: 'Agra', cities: ['Agra City', 'Tajganj', 'Fatehabad', 'Kiraoii'] },
          { name: 'Varanasi', cities: ['Varanasi City', 'Sarnath', 'Pindra', 'Ramnagar'] },
          { name: 'Kanpur Nagar', cities: ['Kanpur City', 'Kalyanpur', 'Civil Lines', 'Jajmau'] },
          { name: 'Prayagraj', cities: ['Prayagraj City', 'Civil Lines', 'Naini', 'Phulpur'] },
          { name: 'Ghaziabad', cities: ['Ghaziabad City', 'Indirapuram', 'Vaishali', 'Modinagar', 'Loni'] }
        ]
      },
      {
        name: 'Kerala',
        districts: [
          { name: 'Ernakulam', cities: ['Kochi / Cochin', 'Aluva', 'Kakkanad', 'Thrippunithura', 'Perumbavoor', 'Angamaly'] },
          { name: 'Thiruvananthapuram', cities: ['Trivandrum City', 'Kazhakkoottam', 'Neyyattinkara', 'Varkala', 'Attingal'] },
          { name: 'Kozhikode', cities: ['Kozhikode City', 'Vatakara', 'Koyilandy', 'Ramanattukara'] },
          { name: 'Thrissur', cities: ['Thrissur City', 'Guruvayur', 'Chalakudy', 'Irinjalakuda', 'Kunamkulam'] },
          { name: 'Kannur', cities: ['Kannur City', 'Thalassery', 'Payyanur', 'Mattannur'] },
          { name: 'Kollam', cities: ['Kollam City', 'Karunagappally', 'Punalur', 'Paravur'] },
          { name: 'Malappuram', cities: ['Malappuram City', 'Manjeri', 'Perinthalmanna', 'Tirur'] }
        ]
      },
      {
        name: 'Rajasthan',
        districts: [
          { name: 'Jaipur', cities: ['Jaipur City', 'Amer', 'Sanganer', 'Mansarovar', 'Vaishali Nagar', 'Chomu'] },
          { name: 'Jodhpur', cities: ['Jodhpur City', 'Luni', 'Phalodi', 'Bilara'] },
          { name: 'Udaipur', cities: ['Udaipur City', 'Fatehnagar', 'Salumbar'] },
          { name: 'Kota', cities: ['Kota City', 'Ramganj Mandi', 'Sangod'] },
          { name: 'Ajmer', cities: ['Ajmer City', 'Beawar', 'Kishangarh', 'Pushkar'] }
        ]
      },
      {
        name: 'Haryana',
        districts: [
          { name: 'Gurugram', cities: ['Gurugram City', 'Manesar', 'Sohna', 'Pataudi'] },
          { name: 'Faridabad', cities: ['Faridabad City', 'Ballabhgarh', 'NIT Faridabad'] },
          { name: 'Ambala', cities: ['Ambala Cantt', 'Ambala City', 'Naraingarh'] },
          { name: 'Karnal', cities: ['Karnal City', 'Gharaunda', 'Assandh'] },
          { name: 'Panipat', cities: ['Panipat City', 'Samalkha'] }
        ]
      },
      {
        name: 'Punjab',
        districts: [
          { name: 'Amritsar', cities: ['Amritsar City', 'Ajnala', 'Attari', 'Majitha'] },
          { name: 'Ludhiana', cities: ['Ludhiana City', 'Khanna', 'Jagraon', 'Samrala'] },
          { name: 'Jalandhar', cities: ['Jalandhar City', 'Phillaur', 'Nakodar'] },
          { name: 'Patiala', cities: ['Patiala City', 'Nabaha', 'Rajpura'] },
          { name: 'SAS Nagar (Mohali)', cities: ['Mohali City', 'Kharar', 'Zirakpur', 'Derabassi'] }
        ]
      },
      {
        name: 'Madhya Pradesh',
        districts: [
          { name: 'Indore', cities: ['Indore City', 'Mhow', 'Depalpur', 'Sanwer'] },
          { name: 'Bhopal', cities: ['Bhopal City', 'Bairagarh', 'Kolar'] },
          { name: 'Gwalior', cities: ['Gwalior City', 'Dabra', 'Bhitarwar'] },
          { name: 'Jabalpur', cities: ['Jabalpur City', 'Sihora', 'Patan'] },
          { name: 'Ujjain', cities: ['Ujjain City', 'Nagda', 'Khachrod', 'Mahidpur'] }
        ]
      },
      {
        name: 'Bihar',
        districts: [
          { name: 'Patna', cities: ['Patna City', 'Danapur', 'Phulwari Sharif', 'Fatwah', 'Barh'] },
          { name: 'Gaya', cities: ['Gaya City', 'Bodh Gaya', 'Sherghati', 'Tekari'] },
          { name: 'Muzaffarpur', cities: ['Muzaffarpur City', 'Kanti', 'Motipur'] },
          { name: 'Bhagalpur', cities: ['Bhagalpur City', 'Kahlgaon', 'Sultanganj'] }
        ]
      },
      {
        name: 'Odisha',
        districts: [
          { name: 'Khordha', cities: ['Bhubaneswar', 'Khordha City', 'Jatni'] },
          { name: 'Cuttack', cities: ['Cuttack City', 'Choudwar', 'Banki'] },
          { name: 'Ganjam', cities: ['Berhampur', 'Chhatrapur', 'Hinjilicut'] },
          { name: 'Puri', cities: ['Puri City', 'Konark', 'Pipili'] }
        ]
      },
      {
        name: 'Assam',
        districts: [
          { name: 'Kamrup Metropolitan', cities: ['Guwahati', 'Dispur', 'North Guwahati'] },
          { name: 'Dibrugarh', cities: ['Dibrugarh City', 'Tinsukia', 'Naharkatia'] },
          { name: 'Silchar', cities: ['Silchar City', 'Lakhipur'] },
          { name: 'Jorhat', cities: ['Jorhat City', 'Mariani', 'Titabor'] }
        ]
      },
      {
        name: 'Goa',
        districts: [
          { name: 'North Goa', cities: ['Panaji', 'Mapusa', 'Calangute', 'Ponda', 'Bicholim'] },
          { name: 'South Goa', cities: ['Margao', 'Vasco da Gama', 'Quepem', 'Curchorem'] }
        ]
      }
    ]
  }
];