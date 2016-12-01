/* List randomizer for generating randomized class groups.
 * Author: Paul M. Gurniak
 * Date: 1/25/14
 *
 * This randomizer reads in a list of names in a class, and spits
 * out randomized pairings.  In the case of an odd number of names,
 * one group is randomly assigned three members.
 *
 * It is assumed that the list is formatted with one name per line,
 * and no other text.
 */

#include <stdio.h>
#include <stdlib.h>
#include <vector>
#include <fstream>
#include <string>
#include <algorithm>

using namespace std;

const char * filename = NULL;  
unsigned int seed = 0;

int parse_args(int argc, char ** argv)
{
  
  for(int i = 1; i < argc; i++) {
    if(!strncmp(argv[i], "-s", strlen("-r"))) {
      seed = atoi(argv[++i]);
    }
    else {
      filename = argv[i];
    }
  }

  if(!seed) {
    seed = time(NULL);
  }
  if(!filename) {
    fprintf(stderr, "Error: no input file list specified!\n");
    return -1;
  }

  return 0;
}

int main(int argc, char ** argv)
{
  vector<string> name_list;
  if(parse_args(argc, argv)) {
    return 1;
  }
  
  ifstream f(filename);
  if(!f) {
    fprintf(stderr, "Error: unable to open input file (%s)\n", filename);
    return 1;
  }
  fprintf(stderr, "Selected seed is: %u\n", seed);
  fprintf(stderr, "Selected filename: %s\n", filename);
  
  string line;
  while(std::getline(f, line)) {
    name_list.push_back(line);
  }
  
  srand(seed);
  
  // Perform an unbiased Fisher-Yates shuffle
  for(size_t i = name_list.size(); i > 0; i--) {
    size_t swap_i = rand() % i;
    string tmp = name_list.at(swap_i);
    name_list.at(swap_i) = name_list.at(i-1);
    name_list.at(i-1) = tmp;
  }
  
  // Print formatted HTML output to stdout
  printf("<html>\n");
  printf("<head>\n");
  printf("<title>\n");
  printf("ESE 171 Group Assignments\n");
  printf("</title>\n");
  printf("</head>\n");
  printf("<body>\n");
  printf("<h1>Group Assignment</h1>\n");
  printf("<p>\n");
  printf("<table border=\"1\">\n");
  printf("<tr><td><b>STUDENT NAME (pennkey)</b></td><td><b>Group Number</b></td></tr>\n");
  
  int groupNum = 1;
  bool oddcount = (name_list.size() & 1) == 0x1LU;
  for(size_t i = 0; i < name_list.size(); i++) {
    groupNum = ((oddcount && i >= 3 && (i & 1)) || ((i >= 2) && !(oddcount || (i & 1)))) ? groupNum + 1 : groupNum;
    printf("<tr><td>%s</td><td>%d</td></tr>\n", name_list.at(i).c_str(), groupNum);
  }
  printf("</table>\n");
  printf("</p>\n");
  printf("<p>\n");
  printf("This random listing generated using the following program and classlist:<br>\n");
  printf("Program: <a href=\"randclass.cpp\">randclass.cpp</a><br>\n");
  printf("Class list: <a href=\"%s\">%s</a><br>\n", filename, filename);
  printf("Rand seed: %u<br>\n", seed);
  printf("Usage: randclass -s %u %s<br>\n", seed, filename);
  printf("</p>\n");
  printf("</body>\n");
  printf("</html>\n");

  return 0;
}
