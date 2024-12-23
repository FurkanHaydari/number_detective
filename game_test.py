import random


numbers=[1,2,3,4,5,6,7,8,9,0]


class Game:
    def __init__(self):
        self.path_selection = random.choice([1, 2, 3])

        self.first_a_choices = ["xax","xxa"]
        self.first_b_choices = ["bxx","xxb"]
        self.first_c_choices = ["cxx","xcx"]

        self.only_a_and_it_is_correct_choices = ["axx"]
        self.only_b_and_it_is_correct_choices = ["xbx"]
        self.only_c_and_it_is_correct_choices = ["xxc"]

        self.ab_false = ["bax","bxa","xab"]
        self.ac_false = ["cax","xca","cxa"]
        self.cb_false = ["bcx","cxb","xcb"]

        self.a_true_but_remain_c_false = ["acx"]
        self.a_true_but_remain_b_false = ["axb"]

        self.b_true_but_remain_a_false = ["xba"]
        self.b_true_but_remain_c_false = ["cbx"]
        
        self.c_true_but_remain_a_false = ["xac"]
        self.c_true_but_remain_b_false = ["bxc"]

        self.first_choices,self.second_choices,self.third_choices,self.fourth_choices,self.fifth_choices=[],[],[],[],[]
        self.first_hint,self.second_hint,self.third_hint,self.fourth_hint,self.fifth_hint="","","","",""
    
    def play(self):
        self.generate_number_with_3_digits()
        self.generate_hint_choices()
        self.generate_hints()
        self.display_hints()
        self.generate_readable_hints()
        self.display_hints()
        # self.get_user_input()
        # self.check_user_input()

    def generate_number_with_3_digits(self):
        self.hundred_digit = random.choice(numbers)
        numbers.remove(self.hundred_digit)
        self.ten_digit = random.choice(numbers)
        numbers.remove(self.ten_digit)
        self.one_digit = random.choice(numbers)
        numbers.remove(self.one_digit)
        self.number = self.hundred_digit*100 + self.ten_digit*10 + self.one_digit
        print(self.number)

    def generate_hint_choices(self):
#   /*

#     İpucu Sistemi:

#     Örnek Gizli Sayı: 123


#     İpucu No	        İpucu Açıklaması	                                                            Örnek İpucu	Kurallar
#       1	        1 rakam doğru ama yanlış yerde (xx1)		                            Rastgele bir rakam seçilir ve yanlış konumda verilir. 
#                                                                                                   (A, B veya C’den biri seçilir.)
#
#       2	        1 rakam doğru ve doğru yerde	(1xx)	                                İlk ipucunda kullanılan rakam doğru yerine yerleştirilir.
#                                                                                               Diğer 2 rakam doğrudur ama yanlış konumda verilir.
#
#       3	        2 rakam doğru ama ikisi de yanlış yerde (x12)	                        Önceki ipuçlarında olmayan yeni bir ‘X’ rakamı eklenir.
#
#
#       4	        2 rakam doğru ama ikisi de yanlış yerde (34x)	                	Başka bir ‘X’ rakamı eklenir ve farklı 2 rakam yanlış yerde belirtilir.
#
#
#       5	        2 rakam doğru, biri doğru yerde, biri yanlış yerde (x21)	       	            Önceki ‘X’ rakamlarından farklı bir ‘X’ eklenir. 
#                                                                                            Doğru yerde ve yanlış yerde bulunan rakamlar birlikte verilir.
#
#   */

        temp = random.choice([True, False])

        if self.path_selection == 1:
            self.first_choices=self.first_a_choices
            self.second_choices=self.only_a_and_it_is_correct_choices
            self.third_choices=(self.ab_false if temp is True else self.ac_false)
            self.fourth_choices=self.cb_false
            self.fifth_choices=self.b_true_but_remain_c_false if temp is True else self.c_true_but_remain_b_false
        
        elif self.path_selection == 2:
            self.first_choices= self.first_b_choices
            self.second_choices= self.only_b_and_it_is_correct_choices
            self.third_choices=(self.ab_false if temp is True else self.cb_false)
            self.fourth_choices=self.ac_false
            self.fifth_choices=self.a_true_but_remain_c_false if temp is True else self.c_true_but_remain_a_false
        else:
            self.first_choices= self.first_c_choices
            self.second_choices= self.only_c_and_it_is_correct_choices
            self.third_choices=(self.ac_false if temp is True else self.cb_false)
            self.fourth_choices=self.ab_false
            self.fifth_choices=self.a_true_but_remain_b_false if temp is True else self.b_true_but_remain_a_false


    def generate_hints(self):
        self.first_hint = random.choice(self.first_choices)
        self.second_hint = random.choice(self.second_choices)
        self.third_hint = random.choice(self.third_choices)
        self.fourth_hint = random.choice(self.fourth_choices)
        self.fifth_hint = random.choice(self.fifth_choices)


    def generate_readable_hints(self):
        self.first_hint = self.make_readable(self.first_hint)
        self.second_hint = self.make_readable(self.second_hint)
        self.third_hint = self.make_readable(self.third_hint)
        self.fourth_hint = self.make_readable(self.fourth_hint)
        self.fifth_hint = self.make_readable(self.fifth_hint)

    def make_readable(self, hint):
        readable_hint = ""
        for i in range(len(hint)):
            # Create a dictionary that maps each character to its corresponding action
            switch = {
                "x": lambda: str(random.choice(numbers)),
                "a": lambda: str(self.hundred_digit),
                "b": lambda: str(self.ten_digit),
                "c": lambda: str(self.one_digit),
            }
            
            # Check if the current character exists in the switch dictionary
            if hint[i] in switch:
                temp = switch[hint[i]]()  # Call the function from the dictionary
                if hint[i] == "x":
                    numbers.remove(int(temp))  # Remove the number if it's 'x'
                readable_hint += temp
        
        return readable_hint


    def display_hints(self):
        print(f"First hint: {self.first_hint} -> One number is correct but wrongly placed.")
        print(f"Second hint: {self.second_hint} -> One numbers are correct and correctly placed.")
        print(f"Third hint: {self.third_hint}" + " -> Two numbers are correct but wrongly placed.")
        print(f"Fourth hint: {self.fourth_hint}" + " -> Two numbers are correct but wrongly placed.")
        print(f"Fifth hint: {self.fifth_hint}" + " -> Two numbers are correct but one of them is correctly placed.")


def main():
    # we somehow need to create a game object
    game = Game()
    game.play()


if __name__ == '__main__':
    main()